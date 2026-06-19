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

    private MainFrame parent; // Declare a variable to store frame address to avoid create too much frame

    // Created gamePanel as an anonymous subclass to override paint() for custom path drawing
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
            super.paint(g); // Draw all grid buttons first

            // Check if there is a valid path to draw
            if (currentPath != null && currentPath.size() > 1) {
                Graphics2D g2d = (Graphics2D) g;

                // Enable anti-aliasing for smooth lines
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Set line properties: Red color, 5px thickness, rounded caps and joins
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Dynamically calculate the dimensions of a single cell in the grid
                int cellWidth = this.getWidth() / cols;
                int cellHeight = this.getHeight() / rows;

                // Iterate through the path points to draw connecting lines
                for (int i = 0; i < currentPath.size() - 1; i++) {
                    int[] p1 = currentPath.get(i);
                    int[] p2 = currentPath.get(i + 1);

                    // Calculate center pixel coordinates for the current node (p[1] is col, p[0] is row)
                    int x1 = (p1[1] * cellWidth) + (cellWidth / 2);
                    int y1 = (p1[0] * cellHeight) + (cellHeight / 2);

                    // Calculate center pixel coordinates for the next node
                    int x2 = (p2[1] * cellWidth) + (cellWidth / 2);
                    int y2 = (p2[0] * cellHeight) + (cellHeight / 2);

                    // Draw the line segment
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
    };

    private GameLogic gameLogic; // clas logic to handle logic in this game
    private int numberOfImage; // the total of image in a level
    private int progress; // the number of image pairs that player chose
    private int currentLevel = 1;
    private int maxLevel = 9;

    private int rows, cols; // the number of rows and cols in game
    private String difficulty; // the difficulity to check archievement
    private PokemonNode firstSelected = null;

    // NEW FIELD: Stores the coordinates of the path to be drawn
    private ArrayList<int[]> currentPath = null; // the array to store the path of the shortest path

    private int score = 0; // player's score
    private int swapsAvailable = 3; // the times that user can use to change matrix to continue
    private boolean isMuted = false;

    // child label to hold game property
    private JLabel scoreLabel;
    private JLabel levelLabel;
    private JLabel swapsLabel;
    private JButton btnMute;

    private JProgressBar timeBar;
    private Timer countdownTimer;
    private int timeLeft;
    private final int MAX_TIME = 600;

    /**
     * Constructs the InGame panel.
     *
     * @param x The number of rows in the matrix.
     * @param y The number of columns in the matrix.
     * @param difficulty The difficulty of the game
     * @param parent The main frame acting as the parent container.
     */
    public InGame(int x, int y, String difficulty, MainFrame parent) {
        this.parent = parent;
        this.rows = x + 2;
        this.cols = y + 2;
        this.difficulty = difficulty;
        // Calculate total number of images based on grid size
        this.numberOfImage = x * y;

        this.gameLogic = new GameLogic(x, y);
        gamePanel.setLayout(new GridLayout(x + 2, y + 2, 0, 0));
        initUI();
    }

    /**
     * Initializes the user interface for the game panel.
     */
    private void initUI() {
        this.setLayout(new BorderLayout());

        // Declare panel to show player progress such as current level, score, number of available swaps
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(100, 900));
        leftPanel.setBackground(new Color(230, 230, 230));
        leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 20));

        levelLabel = new JLabel("Level: " + currentLevel);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        swapsLabel = new JLabel("Swaps: " + swapsAvailable);
        swapsLabel.setFont(new Font("Arial", Font.BOLD, 14));

        leftPanel.add(levelLabel);
        leftPanel.add(scoreLabel);
        leftPanel.add(swapsLabel);
        this.add(leftPanel, BorderLayout.WEST);

        // Declare panel to show tiem bar, game ui and buttons use in game
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(1100, 900));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        timeBar = new JProgressBar(0, MAX_TIME);
        timeBar.setValue(MAX_TIME);
        timeBar.setPreferredSize(new Dimension(1260, 25));
        timeBar.setStringPainted(true);
        timeBar.setForeground(Color.GREEN);
        rightPanel.add(timeBar, BorderLayout.NORTH);

        // Declare an outer for in game panel to keep it alway in the center
        JPanel gridWrapper = new JPanel(new GridBagLayout());
        gridWrapper.add(gamePanel);
        rightPanel.add(gridWrapper, BorderLayout.CENTER);

        JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));

        // Determine initial text for mute button based on state
        btnMute = new JButton(isMuted ? "Unmute" : "Mute");
        JButton btnSwap = new JButton("Swap Matrix");
        JButton btnNewGame = new JButton("New Game");
        JButton btnMenu = new JButton("Menu");

        btnMute.addActionListener(e -> {
            // Toggle muted state and update button text
            isMuted = !isMuted;
            btnMute.setText(isMuted ? "Unmute" : "Mute");
        });

        btnSwap.addActionListener(e -> {
            // Check if there are swaps available
            if (swapsAvailable > 0) {
                swapsAvailable--;
                swapsLabel.setText("Swaps: " + swapsAvailable);
                level(gameLogic.swapMatrix());
                playSound("sound4.wav");
            } else {
                JOptionPane.showMessageDialog(this, "No swaps available!");
            }
        });

        btnNewGame.addActionListener(e -> {
            InGame newGame = new InGame(rows - 2, cols - 2, difficulty, parent);
            parent.switchPanel(newGame);
        });

        btnMenu.addActionListener(e -> {
            // Stop the countdown timer if it is running
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            MenuUI menu = new MenuUI(parent);
            parent.switchPanel(menu);
        });

        bottomButtonsPanel.add(btnMute);
        bottomButtonsPanel.add(btnSwap);
        bottomButtonsPanel.add(btnNewGame);
        bottomButtonsPanel.add(btnMenu);
        rightPanel.add(bottomButtonsPanel, BorderLayout.SOUTH);

        this.add(rightPanel, BorderLayout.CENTER);

        // after set up all backgroud the system call startgame to create matrix and map image show to the player
        startGame();
    }

    /**
     * Sets up and starts the countdown timer for the level.
     */
    private void setupTimer() {
        // Stop the existing timer if it is currently running
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        timeLeft = MAX_TIME;
        timeBar.setValue(timeLeft);

        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            timeBar.setValue(timeLeft);
            // check time left to change the time bar color
            if (timeLeft <= (MAX_TIME - 2 * MAX_TIME / 10) && timeLeft > (MAX_TIME - 7 * MAX_TIME / 10)) {
                timeBar.setForeground(Color.YELLOW);
            } else if (timeLeft < (MAX_TIME - 7 * MAX_TIME / 10)) {
                timeBar.setForeground(Color.RED);
            }

            // Trigger game over if the time runs out
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
        JOptionPane.showMessageDialog(parent, "Time's up! You lose.");
        MenuUI menu = new MenuUI(parent);
        parent.switchPanel(menu);
    }

    /**
     * Starts the game by resetting necessary stats and generating the matrix.
     */
    private void startGame() {
        // reset swapsAvailable value
        swapsAvailable = 3;
        swapsLabel.setText("Swaps: " + swapsAvailable);
        levelLabel.setText("Level: " + currentLevel);

        // call gameLogic to create and get matrix
        gameLogic.createMatrix();
        progress = 0;

        // start to map image and start the game
        level(gameLogic.getMatrix());
        playSound("sound4.wav");

        setupTimer();
    }

    /**
     * Sets up the game interface for the current level based on the provided
     * matrix.
     *
     * @param matrix The 2D array representing the game state.
     */
    private void level(int[][] matrix) {
        gamePanel.removeAll();
        firstSelected = null;

        // Reset the path array when loading/reloading the level matrix
        currentPath = null;

        int baseWidth = 40;
        int baseHeight = 50;

        double maxAvailableWidth = 1200.0;
        double maxAvailableHeight = 700.0;

        // Calculate the scale ratios for width and height
        double scaleX = (maxAvailableWidth / cols) / baseWidth;
        double scaleY = (maxAvailableHeight / rows) / baseHeight;

        // Determine the minimum scale to ensure it fits within the bounds
        double scale = Math.min(scaleX, scaleY);

        // Constrain the scale factor between a minimum of 0.75 and a maximum of 1.5
        scale = Math.max(0.75, Math.min(1.5, scale));

        // Calculate final width and height dimensions using the constrained scale
        int finalWidth = (int) (baseWidth * scale);
        int finalHeight = (int) (baseHeight * scale);
        Dimension dynamicBtnSize = new Dimension(finalWidth, finalHeight);

        ImageIcon[] pieceIcons = new ImageIcon[36];

        // Loop through to load and scale all 36 piece icons
        for (int i = 0; i < 36; i++) {
            String fileName = "pieces" + (i + 1) + ".png";
            java.net.URL imgURL = getClass().getResource("/pikachu/img/" + fileName);

            // Check if the image URL is valid before applying scale
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImg = originalIcon.getImage().getScaledInstance(finalWidth, finalHeight, Image.SCALE_SMOOTH);
                pieceIcons[i] = new ImageIcon(scaledImg);
            }
        }

        int row = -1;

        // Loop through the matrix grid to initialize buttons
        for (int i = 0; i < rows * cols; i++) {
            // Calculate column index based on the current loop iteration
            int col = i % cols;

            // Increment row when column wraps around to 0
            if (col == 0) {
                row++;
            }

            int currentImgID = matrix[row][col];
            PokemonNode btn = new PokemonNode(row, col, currentImgID);

            // Verify if the current image ID is valid to show the icon, else hide the button
            if (currentImgID != -1) {
                btn.setIcon(pieceIcons[currentImgID]);
            } else {
                btn.setVisible(false);
            }

            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusPainted(false);

            btn.setPreferredSize(dynamicBtnSize);
            btn.setMinimumSize(dynamicBtnSize);
            btn.setMaximumSize(dynamicBtnSize);

            gamePanel.add(btn);

            btn.addActionListener(e -> {
                PokemonNode currentClick = (PokemonNode) e.getSource();

                // If this is the first button selected
                if (firstSelected == null) {
                    firstSelected = currentClick;
                    firstSelected.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    playSound("sound2.wav");
                } // If the user clicks the currently selected button again
                else if (firstSelected == currentClick) {
                    firstSelected.setBorder(BorderFactory.createEmptyBorder());
                    firstSelected = null;
                    playSound("sound1.wav");
                } // If a second different button is clicked
                else {
                    int id1 = firstSelected.getImageID();
                    int id2 = currentClick.getImageID();

                    // Check if both selected buttons have the same image ID
                    if (id1 == id2) {
                        // Capture the returning path list from gameLogic instead of boolean
                        ArrayList<int[]> path = gameLogic.findPath(firstSelected, currentClick);

                        // If a valid path exists between the two nodes (array is not null)
                        if (path != null) {
                            // Add 20 points for a successful connection
                            score += 20;

                            // SET THE PATH TO BE DRAWN AND REPAINT
                            currentPath = path;
                            gamePanel.repaint();

                            PokemonNode node1 = firstSelected;
                            PokemonNode node2 = currentClick;

                            // DELAY: Increased from 150 to 400 to let the player clearly see the drawn red line
                            Timer timer = new Timer(150, t -> {
                                currentPath = null; // Clear the drawn path
                                level(gameLogic.updateMatrix(node1.getRow(), node1.getCol(), node2.getRow(), node2.getCol(), currentLevel));

                                // If there are no valid moves left on the board, force a swap
                                if (!gameLogic.checkValidMatrix() && progress < numberOfImage) {
                                    level(gameLogic.swapMatrix());
                                    playSound("sound4.wav");
                                }
                            });

                            timer.setRepeats(false);
                            timer.start();

                            playSound("sound5.wav");
                            // Increment progress by 2 since a pair is cleared
                            progress += 2;

                            // If the board is fully cleared
                            if (progress >= numberOfImage) {
                                handleLevelComplete();
                            }
                        } // If path is invalid
                        else {
                            if (score > 0) {
                                score -= 10;
                            }
                            firstSelected.setBorder(BorderFactory.createEmptyBorder());
                            playSound("sound1.wav");
                        }
                    } // If the IDs do not match
                    else {
                        if (score > 0) {
                            score -= 10;
                        }
                        playSound("sound1.wav");
                        firstSelected.setBorder(BorderFactory.createEmptyBorder());
                    }
                    scoreLabel.setText("Score: " + score);
                    firstSelected = null;
                }
            });
        }

        gamePanel.revalidate();
        gamePanel.repaint();
    }

    /**
     * Handles the transition logic when a level is successfully completed.
     */
    private void handleLevelComplete() {
        // Stop the countdown timer to prevent overlaps before starting the next level
        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        currentLevel++;

        // Check if there are more levels remaining
        if (currentLevel <= maxLevel) {
            JOptionPane.showMessageDialog(parent, "Congratulations! You finished level " + (currentLevel - 1));
            startGame();
        } // If all levels are completed
        else {
            JOptionPane.showMessageDialog(parent, "Congratulations! You finished the game!");
            Achievement achievement = new Achievement(score, difficulty);
            AchievementManager manager = new AchievementManager();
            manager.addAchievement(achievement);

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
        // Return immediately if the mute flag is active
        if (isMuted) {
            return;
        }

        try {
            String soundPath = "/pikachu/sound/" + soundFileName;
            java.net.URL soundURL = InGame.class.getResource(soundPath);

            // Verify if the sound file exists
            if (soundURL != null) {
                javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(soundURL);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } else {
                System.out.println("Error: Can not find sound with path: " + soundFileName);
            }
        } catch (Exception e) {
            System.out.println("Can not play sound: " + e.getMessage());
        }
    }
}
