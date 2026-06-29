package pikachu.ui;

import pikachu.logic.GameLogic;
import pikachu.logic.PokemonNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import pikachu.data.Achievement;
import pikachu.data.AchievementManager;

/**
 * The InGame class represents the main game panel where the puzzle is displayed
 * and played.
 */
public class InGame extends JPanel {

    // Reference to the main application frame to allow panel switching without instantiating new frames
    private MainFrame parent;

    // Custom JPanel used for the game board to allow custom painting (drawing the red connection path)
    private JPanel gamePanel = new JPanel() {
        /**
         * Overrides the paint method to draw the path line over the buttons.
         * The standard components (buttons) are drawn first, then the path is
         * rendered on top.
         *
         * @param g The Graphics object used for drawing.
         */
        @Override
        public void paint(Graphics g) {
            super.paint(g); // Step 1: Draw all child components (the grid buttons) first

            // Step 2: Check if there is a valid path to draw and it has at least 2 points to connect
            if (currentPath != null && currentPath.size() > 1) {
                Graphics2D g2d = (Graphics2D) g;

                // Enable anti-aliasing to prevent jagged edges on the red line
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Set line style: Red color, 5px thickness, with rounded caps and joins for a smoother look
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Dynamically calculate the pixel dimensions of a single cell in the grid
                int cellWidth = this.getWidth() / cols;
                int cellHeight = this.getHeight() / rows;

                // Iterate through the path list to draw connecting line segments between consecutive nodes
                for (int i = 0; i < currentPath.size() - 1; i++) {
                    int[] p1 = currentPath.get(i); // Current node
                    int[] p2 = currentPath.get(i + 1); // Next node

                    // Calculate center pixel coordinates for the current node (p[1] is col -> X axis, p[0] is row -> Y axis)
                    int x1 = (p1[1] * cellWidth) + (cellWidth / 2);
                    int y1 = (p1[0] * cellHeight) + (cellHeight / 2);

                    // Calculate center pixel coordinates for the next node
                    int x2 = (p2[1] * cellWidth) + (cellWidth / 2);
                    int y2 = (p2[0] * cellHeight) + (cellHeight / 2);

                    // Render the line segment on the screen
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
    };

    // --- Core Game Logic & State Variables ---
    private GameLogic gameLogic;         // Handles algorithms (pathfinding, matrix generation, etc.)
    private int numberOfImage;           // Total number of images (pairs) in the current level
    private int progress;                // Number of individual images cleared so far
    private int currentLevel = 1;        // Tracks the current level number
    private int maxLevel = 9;            // Maximum level before finishing the game

    private int rows, cols;              // Number of rows and columns (including the empty outer borders)
    private String difficulty;           // Difficulty setting used for recording achievements
    private ImageIcon[] pieceIcons = new ImageIcon[36]; // An array store images

    // --- User Interaction Variables ---
    private PokemonNode firstSelected = null; // Stores the reference to the first clicked button
    private JButton firstButton = null;       // Auxiliary reference to handle button disabling
    private ArrayList<int[]> currentPath = null; // Stores the coordinates of the shortest path to be drawn

    // --- Game Stats & UI Components ---
    private int score = 0;               // Player's current score
    private int swapsAvailable = 10;      // Number of matrix shuffles available to the player
    private boolean isMuted = false;     // Flag to control sound effects

    private JLabel scoreLabel;
    private JLabel levelLabel;
    private JLabel swapsLabel;
    private JButton btnMute;

    // --- Timer Components ---
    private JProgressBar timeBar;
    private Timer countdownTimer;
    private int timeLeft;
    private final int MAX_TIME = 600;     // Maximum time allowed per level (in seconds)
    
    // --- Background ---
    private Image backgroundImage;

    /**
     * Constructs the InGame panel.
     *
     * @param x The number of rows in the matrix (excluding borders).
     * @param y The number of columns in the matrix (excluding borders).
     * @param difficulty The difficulty of the game.
     * @param parent The main frame acting as the parent container.
     */
    public InGame(int x, int y, String difficulty, MainFrame parent) {
        this.parent = parent;
        this.isMuted = parent.isMuted();

        // Add 2 to rows and cols to account for the outer empty borders required for pathfinding
        this.rows = x + 2;
        this.cols = y + 2;
        this.difficulty = difficulty;

        // Total number of playable images (excluding borders)
        this.numberOfImage = x * y;

        this.gameLogic = new GameLogic(x, y);

        // Set layout for the game panel to a grid matching the matrix size
        gamePanel.setLayout(new GridLayout(this.rows, this.cols, 0, 0));
        gamePanel.setOpaque(false);

        try {
            java.net.URL bgURL = getClass().getResource("/pikachu/img/game_background.png");
            if (bgURL == null) {
                bgURL = getClass().getResource("/img/game_background.png");
            }
            if (bgURL != null) {
                backgroundImage = new ImageIcon(bgURL).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getImage();
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    /**
     * Initializes the user interface for the game panel, dividing it into
     * sections.
     */
    private void initUI() {
        this.setLayout(new BorderLayout());

        // --- Left Panel: Displays player stats (Level, Score, Swaps) ---
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 150)); // Semi-transparent dark background for visibility
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        leftPanel.setPreferredSize(new Dimension(150, 900));
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 30));

        levelLabel = new JLabel("Level: " + currentLevel);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 20));
        levelLabel.setForeground(Color.WHITE);

        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);

        swapsLabel = new JLabel("Swaps: " + swapsAvailable);
        swapsLabel.setFont(new Font("Arial", Font.BOLD, 20));
        swapsLabel.setForeground(Color.WHITE);

        leftPanel.add(levelLabel);
        leftPanel.add(scoreLabel);
        leftPanel.add(swapsLabel);
        this.add(leftPanel, BorderLayout.WEST);

        // --- Right Panel: Contains the Time Bar, Game Board, and Action Buttons ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(1100, 900));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        rightPanel.setOpaque(false);

        // Time Bar setup at the top of the right panel
        timeBar = new JProgressBar(0, MAX_TIME * 1000) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Draw dark background
                g2d.setColor(new Color(30, 30, 30, 150));
                g2d.fillRoundRect(0, 0, w, h, 15, 15);
                
                int fillWidth = (int) (w * getPercentComplete());
                if (fillWidth > 0) {
                    Color startColor = new Color(0, 200, 0); // Green
                    Color endColor = new Color(150, 255, 150);
                    
                    GradientPaint gp = new GradientPaint(0, 0, startColor, 0, h, endColor);
                    g2d.setPaint(gp);
                    g2d.fillRoundRect(0, 0, fillWidth, h, 15, 15);
                }
                
                g2d.dispose();
            }
        };
        timeBar.setValue(MAX_TIME * 1000);
        timeBar.setPreferredSize(new Dimension(1260, 25));
        timeBar.setStringPainted(false);
        timeBar.setOpaque(false);
        timeBar.setBorder(BorderFactory.createEmptyBorder());
        rightPanel.add(timeBar, BorderLayout.NORTH);

        // Wrapper panel to keep the game board centered
        JPanel gridWrapper = new JPanel(new GridBagLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(gamePanel);
        rightPanel.add(gridWrapper, BorderLayout.CENTER);

        // --- Bottom Panel: Action buttons (Mute, Swap, New Game, Menu) ---
        JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomButtonsPanel.setOpaque(false);

        btnMute = new JButton(isMuted ? "Unmute" : "Mute");
        JButton btnSwap = new JButton("Swap Matrix");
        JButton btnNewGame = new JButton("New Game");
        JButton btnMenu = new JButton("Menu");

        styleButton(btnMute);
        styleButton(btnSwap);
        styleButton(btnNewGame);
        styleButton(btnMenu);

        // Action: Toggle Sound
        btnMute.addActionListener(e -> {
            isMuted = !isMuted;
            parent.setMuted(isMuted);
            btnMute.setText(isMuted ? "Unmute" : "Mute");
        });

        // Action: Shuffle the board manually
        btnSwap.addActionListener(e -> {
            if (swapsAvailable > 0) {
                swapsAvailable--;
                score -= 50;
                scoreLabel.setText("Score: " + score);
                swapsLabel.setText("Swaps: " + swapsAvailable);
                level(gameLogic.swapMatrix()); // Reload the level with the new matrix
                playSound("sound4.wav");
            } else {
                playSound("sound1.wav");
                JOptionPane.showMessageDialog(this, "No swaps available!");
            }
        });

        // Action: Restart the entire game
        btnNewGame.addActionListener(e -> {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            // Create a new instance with original grid dimensions (subtracting the borders added earlier)
            InGame newGame = new InGame(rows - 2, cols - 2, difficulty, parent);
            parent.switchPanel(newGame);
        });

        // Action: Open the pause/menu dialog
        btnMenu.addActionListener(e -> {
            if (countdownTimer != null) {
                countdownTimer.stop(); // Pause the timer while in menu
            }
            menuOptions();
        });

        bottomButtonsPanel.add(btnMute);
        bottomButtonsPanel.add(btnSwap);
        bottomButtonsPanel.add(btnNewGame);
        bottomButtonsPanel.add(btnMenu);
        rightPanel.add(bottomButtonsPanel, BorderLayout.SOUTH);

        this.add(rightPanel, BorderLayout.CENTER);

        // Initialize level data and start the gameplay loop
        startGame();
    }

    private void styleButton(JButton button) {
        button.setPreferredSize(new Dimension(160, 45));
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(255, 204, 0));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
    }

    /**
     * Displays an option dialog to pause the game and choose between continuing
     * or going to the main menu.
     */
    private void menuOptions() {
        String[] options = {"Continue", "Main Menu"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                "",
                "Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0 || choice == -1) {
            // Resume timer if "Continue" or "Close" is chosen
            countdownTimer.start();
        } else if (choice == 1) {
            // Switch to Main Menu if "Main Menu" is chosen
            MenuUI menu = new MenuUI(parent);
            parent.switchPanel(menu);
        }
    }

    /**
     * Plays a sound file given its file name.
     *
     * @param soundFileName The name of the sound file to be played.
     */
    public void playSound(String soundFileName) {
        parent.playSound(soundFileName);
    }

    /**
     * Sets up and starts the countdown timer for the current level.
     */
    private void setupTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        timeLeft = MAX_TIME * 1000;
        timeBar.setMaximum(MAX_TIME * 1000);
        timeBar.setValue(timeLeft);

        countdownTimer = new Timer(50, e -> {
            timeLeft -= 50;
            timeBar.setValue(timeLeft);

            // Trigger Game Over logic when time hits zero
            if (timeLeft <= 0) {
                countdownTimer.stop();
                handleGameOver();
            }
        });
        countdownTimer.start();
    }

    /**
     * Handles the logic when the game is over (e.g., time runs out).
     */
    private void handleGameOver() {
        playSound("sound1.wav");
        JOptionPane.showMessageDialog(parent, "Time's up! You lose.");
        MenuUI menu = new MenuUI(parent);
        parent.switchPanel(menu);
    }

    /**
     * Starts the game by resetting necessary stats and generating the matrix.
     */
    private void startGame() {
        swapsLabel.setText("Swaps: " + swapsAvailable);
        levelLabel.setText("Level: " + currentLevel);

        currentPath = null;
        gameLogic.createMatrix();
        progress = 0;

        // Map the generated matrix to UI components
        level(gameLogic.getMatrix());
        playSound("sound4.wav");

        setupTimer();
    }

    /**
     * Loads Pokémon piece images from the resources folder and scales them dynamically
     * to fit the grid layout based on the available screen space. The scaled images
     * are stored in the pieceIcons array for rendering on the board.
     */
    private void getImage() {
        // --- Dynamic Scaling Logic ---
        int baseWidth = 40;
        int baseHeight = 50;
        double maxAvailableWidth = 1200.0;
        double maxAvailableHeight = 700.0;

        // Calculate the scale ratios needed to fit the grid into the available screen space
        double scaleX = (maxAvailableWidth / cols) / baseWidth;
        double scaleY = (maxAvailableHeight / rows) / baseHeight;

        // Use the minimum scale to ensure no components are cut off
        double scale = Math.min(scaleX, scaleY);

        // Constrain the scale factor to prevent items from being too tiny or overly large
        scale = Math.max(0.75, Math.min(1.5, scale));

        // Apply scale to dimensions
        int finalWidth = (int) (baseWidth * scale);
        int finalHeight = (int) (baseHeight * scale);

        for (int i = 0; i < 36; i++) {
            String fileName = "pieces" + (i + 1) + ".png";
            java.net.URL imgURL = getClass().getResource("/pikachu/img/" + fileName);

            if (imgURL != null) {
                // Scale the image smoothly to match the dynamically calculated button size
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImg = originalIcon.getImage().getScaledInstance(finalWidth, finalHeight, Image.SCALE_SMOOTH);
                pieceIcons[i] = new ImageIcon(scaledImg);
            }
        }
    }

    /**
     * Sets up the game interface for the current level based on the provided
     * matrix. This method dynamically scales images and maps them to the grid
     * layout.
     *
     * @param matrix The 2D array representing the game state.
     */
    private void level(int[][] matrix) {
        // Clear previous board state
        gamePanel.removeAll();
        firstSelected = null;

        // --- Dynamic Scaling Logic ---
        int baseWidth = 40;
        int baseHeight = 50;
        double maxAvailableWidth = 1200.0;
        double maxAvailableHeight = 700.0;

        // Calculate the scale ratios needed to fit the grid into the available screen space
        double scaleX = (maxAvailableWidth / cols) / baseWidth;
        double scaleY = (maxAvailableHeight / rows) / baseHeight;

        // Use the minimum scale to ensure no components are cut off
        double scale = Math.min(scaleX, scaleY);

        // Constrain the scale factor to prevent items from being too tiny or overly large
        scale = Math.max(0.75, Math.min(1.5, scale));

        // Apply scale to dimensions
        int finalWidth = (int) (baseWidth * scale);
        int finalHeight = (int) (baseHeight * scale);
        Dimension dynamicBtnSize = new Dimension(finalWidth, finalHeight);

        // --- Grid Population ---
        int row = -1;

        // Loop through the 1D total item count and map to 2D matrix coordinates
        for (int i = 0; i < rows * cols; i++) {
            int col = i % cols; // Calculate current column

            // When column wraps back to 0, move to the next row
            if (col == 0) {
                row++;
            }

            int currentImgID = matrix[row][col];
            PokemonNode btn = new PokemonNode(row, col, currentImgID);

            // -1 indicates an empty space (e.g., border cell or cleared Pokemon)
            if (currentImgID != -1) {
                btn.setIcon(pieceIcons[currentImgID]);
            } else {
                btn.setVisible(false); // Hide the button to create empty space
            }

            // Remove button styling to make images look seamless
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusPainted(false);
            btn.setPreferredSize(dynamicBtnSize);
            btn.setMinimumSize(dynamicBtnSize);
            btn.setMaximumSize(dynamicBtnSize);

            gamePanel.add(btn);

            // --- Click Event Handling for Gameplay ---
            btn.addActionListener(e -> {
                PokemonNode currentClick = (PokemonNode) e.getSource();

                // State 1: No button is currently selected
                if (firstSelected == null) {
                    firstButton = btn;
                    firstSelected = currentClick;
                    firstSelected.setBorder(BorderFactory.createLineBorder(Color.RED, 3)); // Highlight selection
                    playSound("sound2.wav");
                } // State 2: User clicks the exact same button again (Deselect)
                else if (firstSelected == currentClick) {
                    firstSelected.setBorder(BorderFactory.createEmptyBorder());
                    firstSelected = null;
                    playSound("sound1.wav");
                } // State 3: A second, different button is selected (Attempt Connection)
                else {
                    int id1 = firstSelected.getImageID();
                    int id2 = currentClick.getImageID();

                    // Check if both selected buttons have the same image ID
                    if (id1 == id2) {
                        // Request pathfinding algorithm to find a valid route
                        ArrayList<int[]> path = gameLogic.findPath(firstSelected, currentClick);

                        // If a valid path exists (array is not null)
                        if (path != null) {

                            // FIX RACE CONDITION: Disable both buttons to prevent the user from spam-clicking 
                            // them again while the path drawing animation is running.
                            // The disabled icon is set to the normal icon so the visual doesn't turn gray.
                            firstButton.setEnabled(false);
                            firstButton.setDisabledIcon(firstButton.getIcon());
                            btn.setEnabled(false);
                            btn.setDisabledIcon(btn.getIcon());

                            score += 20;

                            // Pass the path to the panel and trigger a repaint to draw the red line
                            currentPath = path;
                            gamePanel.repaint();

                            // Delay action to let the user see the path before clearing the pieces
                            Timer timer = new Timer(150, t -> {
                                currentPath = null; // Remove the red line
                                gamePanel.repaint();
                            });

                            // Save references for the timer execution
                            PokemonNode node1 = firstSelected;
                            PokemonNode node2 = currentClick;

                            progress += 2; // Increment progress (2 pieces removed)
                            // Update logic matrix to mark pieces as cleared, then refresh the UI
                            level(gameLogic.updateMatrix(node1.getRow(), node1.getCol(), node2.getRow(), node2.getCol(), currentLevel));

                            // Deadlock check: If no moves remain, force a matrix shuffle
                            if (!gameLogic.checkValidMatrix() && progress < numberOfImage) {
                                if (swapsAvailable == 0) {
                                    gamePanel.repaint();
                                    handleGameOver();
                                } else {
                                    level(gameLogic.swapMatrix());
                                    swapsAvailable--;
                                    score -= 30;
                                    swapsLabel.setText("Swaps: " + swapsAvailable);
                                    playSound("sound1.wav");
                                }
                            }

                            timer.setRepeats(false);
                            timer.start();

                            playSound("sound5.wav");

                            // Win Condition Check
                            if (progress >= numberOfImage) {
                                handleLevelComplete();
                            }
                        } // Pathfinding failed (Same image, but blocked path)
                        else {
                            if (score > 0) {
                                score -= 10; // Penalty
                            }
                            firstSelected.setBorder(BorderFactory.createEmptyBorder());
                            playSound("sound1.wav");
                        }
                    } // IDs do not match (Different images selected)
                    else {
                        if (score > 0) {
                            score -= 10; // Penalty
                        }
                        playSound("sound1.wav");
                        firstSelected.setBorder(BorderFactory.createEmptyBorder());
                    }

                    // Reset selection state and update UI
                    scoreLabel.setText("Score: " + score);
                    firstSelected = null;
                }
            });
        }

        // Revalidate and repaint to ensure the new grid is fully rendered
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    /**
     * Handles the transition logic when a level is successfully completed.
     */
    private void handleLevelComplete() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        currentLevel++;

        // Proceed to next level if available
        if (currentLevel <= maxLevel) {
            JOptionPane.showMessageDialog(parent, "Congratulations! You finished level " + (currentLevel - 1));
            swapsAvailable++;
            startGame();
        } // All levels completed - Game Finished
        else {
            JOptionPane.showMessageDialog(parent, "Congratulations! You finished the game!");

            // Save the player's achievement
            Achievement achievement = new Achievement(score, difficulty);
            AchievementManager manager = new AchievementManager();
            manager.addAchievement(achievement);

            // Return to main menu
            MenuUI menu = new MenuUI(parent);
            parent.switchPanel(menu);
        }
    }
}
