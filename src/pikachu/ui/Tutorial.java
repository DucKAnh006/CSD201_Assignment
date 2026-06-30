package pikachu.ui;

import pikachu.logic.GameLogic;
import pikachu.logic.PokemonNode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import pikachu.data.Achievement;
import pikachu.data.AchievementManager;

/**
 * The InGame class represents the main game panel where the puzzle is displayed
 * and played.
 */
public class Tutorial extends JPanel {

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
    private JButton btnSwap;
    private JButton btnNewGame;
    private JButton btnMenu;
    private javax.swing.border.Border defaultButtonBorder;

    // --- Timer Components ---
    private JProgressBar timeBar;
    private final int MAX_TIME = 600;     // Maximum time allowed per level (in seconds)
    
    // --- Background ---
    private Image backgroundImage;

    // --- Tutorial UI Components ---
    private JPanel overlayPanel;
    private JTextArea dialogText;
    private JButton btnSkip;
    private JButton btnNext;
    private JButton btnReview;
    private JButton btnReturnMenu;
    private int currentTutorialStep = 0;
    private String[] tutorialSteps = {
        "Hello! Welcome to Pikachu Classic. Let me show you how to play!",
        "On the left corner. First is the Level information.",
        "Second is your score during the game (Score).",
        "And finally, the number of times you are allowed to refresh the board (Swaps).",
        "Every time you manually refresh the board or the game detects no valid moves, the images will be shuffled and the available swaps will decrease.",
        "But don't worry, after completing each level, you will be rewarded with an extra swap.",
        "The green bar at the top represents time. You must finish the level before the time runs out!",
        "The center section is the game board. Find 2 identical images and click on them to connect.",
        "You can connect 2 images if the path between them has a maximum of 2 turns (3 straight lines).",
        "Let's try it out!",
        "First are the images on the same straight line. Try clicking on this pair!",
        "",
        "Next are images that can be connected with 1 turn. Find and connect them.",
        "",
        "Finally, images that can be connected with 2 turns (up to 3 straight lines).",
        "",
        "Below are the buttons: Mute",
        "Swap Matrix",
        "New Game",
        "And finally, Menu.",
        "Have fun playing the game!"
    };

    /**
     * Constructs the InGame panel.
     *
     * @param x The number of rows in the matrix (excluding borders).
     * @param y The number of columns in the matrix (excluding borders).
     * @param difficulty The difficulty of the game.
     * @param parent The main frame acting as the parent container.
     */
    public Tutorial(MainFrame parent) {
        this.parent = parent;
        this.rows = 11;
        this.cols = 15;
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
        
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);
        
        // --- Base Game Panel ---
        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.setOpaque(false);

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
        basePanel.add(leftPanel, BorderLayout.WEST);

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
        styleButton(btnMute);
        defaultButtonBorder = btnMute.getBorder();
        
        btnSwap = new JButton("Swap Matrix");
        styleButton(btnSwap);
        btnNewGame = new JButton("New Game");
        styleButton(btnNewGame);
        btnMenu = new JButton("Menu");
        styleButton(btnMenu);

        bottomButtonsPanel.add(btnMute);
        bottomButtonsPanel.add(btnSwap);
        bottomButtonsPanel.add(btnNewGame);
        bottomButtonsPanel.add(btnMenu);
        rightPanel.add(bottomButtonsPanel, BorderLayout.SOUTH);

        basePanel.add(rightPanel, BorderLayout.CENTER);

        // --- Tutorial Overlay Panel ---
        overlayPanel = new JPanel(null); // Absolute positioning for overlay elements
        overlayPanel.setOpaque(false);
        // Block mouse events from reaching the game board behind the overlay
        overlayPanel.addMouseListener(new java.awt.event.MouseAdapter() {});

        // Pikachu Image
        JLabel pikachuLabel = new JLabel();
        try {
            java.net.URL imgURL = getClass().getResource("/pikachu/img/pikachu.png");
            if (imgURL == null) {
                imgURL = getClass().getResource("/img/pikachu.png");
            }
            if (imgURL != null) {
                ImageIcon pikaIcon = new ImageIcon(imgURL);
                // Scale to 200x350 to maintain the 530x920 aspect ratio
                Image scaled = pikaIcon.getImage().getScaledInstance(200, 350, Image.SCALE_SMOOTH);
                pikachuLabel.setIcon(new ImageIcon(scaled));
            } else {
                System.out.println("Could not find pikachu1.png");
                pikachuLabel.setText("(Pikachu)"); // Fallback text so we know it's there
                pikachuLabel.setHorizontalAlignment(SwingConstants.CENTER);
                pikachuLabel.setOpaque(true);
                pikachuLabel.setBackground(Color.YELLOW);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Position Pikachu at bottom right
        pikachuLabel.setBounds(950, 480, 200, 350); 
        overlayPanel.add(pikachuLabel);

        // Dialog Box Panel
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBackground(new Color(255, 255, 255)); // Removed transparency
        dialogPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        dialogPanel.setBounds(530, 520, 400, 150); // Left of Pikachu

        dialogText = new JTextArea(tutorialSteps[currentTutorialStep]);
        dialogText.setFont(new Font("Arial", Font.PLAIN, 16));
        dialogText.setWrapStyleWord(true);
        dialogText.setLineWrap(true);
        dialogText.setEditable(false);
        dialogText.setOpaque(false);
        dialogText.setFocusable(false);
        dialogPanel.add(dialogText, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnSkip = new JButton("Skip");
        btnNext = new JButton("Next");
        btnReview = new JButton("Tutorial again");
        btnReturnMenu = new JButton("Return to menu");

        btnReview.setVisible(false);
        btnReturnMenu.setVisible(false);

        btnSkip.addActionListener(e -> endTutorial());
        btnNext.addActionListener(e -> nextTutorialStep());
        
        btnReview.addActionListener(e -> {
            currentTutorialStep = 0;
            dialogText.setText(tutorialSteps[currentTutorialStep]);
            btnReview.setVisible(false);
            btnReturnMenu.setVisible(false);
            btnSkip.setVisible(true);
            btnNext.setVisible(true);
            startGame();
        });
        
        btnReturnMenu.addActionListener(e -> {
            MenuUI menu = new MenuUI(parent);
            parent.switchPanel(menu);
        });

        btnPanel.add(btnSkip);
        btnPanel.add(btnNext);
        btnPanel.add(btnReview);
        btnPanel.add(btnReturnMenu);
        dialogPanel.add(btnPanel, BorderLayout.SOUTH);

        overlayPanel.add(dialogPanel);

        // Add layers: base layer = 0, overlay = 1
        layeredPane.add(basePanel, Integer.valueOf(0));
        layeredPane.add(overlayPanel, Integer.valueOf(1));

        // Ensure both panels resize correctly to fill the layered pane
        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = layeredPane.getWidth();
                int h = layeredPane.getHeight();
                basePanel.setBounds(0, 0, w, h);
                overlayPanel.setBounds(0, 0, w, h);
                basePanel.revalidate();
                basePanel.repaint();
                overlayPanel.revalidate();
                overlayPanel.repaint();
            }
        });

        this.add(layeredPane, BorderLayout.CENTER);

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
     * Clears all highlighting borders from the UI components.
     * This is used to reset the visual focus when moving between tutorial steps.
     */
    private void clearHighlights() {
        if (levelLabel != null) levelLabel.setBorder(BorderFactory.createEmptyBorder());
        if (scoreLabel != null) scoreLabel.setBorder(BorderFactory.createEmptyBorder());
        if (swapsLabel != null) swapsLabel.setBorder(BorderFactory.createEmptyBorder());
        if (timeBar != null) timeBar.setBorder(BorderFactory.createEmptyBorder());
        if (gamePanel != null) gamePanel.setBorder(BorderFactory.createEmptyBorder());
        
        if (btnMute != null && defaultButtonBorder != null) btnMute.setBorder(defaultButtonBorder);
        if (btnSwap != null && defaultButtonBorder != null) btnSwap.setBorder(defaultButtonBorder);
        if (btnNewGame != null && defaultButtonBorder != null) btnNewGame.setBorder(defaultButtonBorder);
        if (btnMenu != null && defaultButtonBorder != null) btnMenu.setBorder(defaultButtonBorder);
    }

    /**
     * Advances the tutorial to the next step.
     * Updates the dialog text, highlights relevant UI components,
     * and sets up specific matrix states for interactive steps.
     */
    private void nextTutorialStep() {
        dialogText.setVisible(true);
        overlayPanel.setVisible(true);
        
        currentTutorialStep++;
        if (currentTutorialStep >= tutorialSteps.length) {
            endTutorial();
        } else {
            dialogText.setText(tutorialSteps[currentTutorialStep]);
            
            clearHighlights();
            if (currentTutorialStep == 1) levelLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            else if (currentTutorialStep == 2) scoreLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            else if (currentTutorialStep >= 3 && currentTutorialStep <= 5) swapsLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            else if (currentTutorialStep == 6) timeBar.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            else if (currentTutorialStep >= 7 && currentTutorialStep <= 9) gamePanel.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            else if (currentTutorialStep == 16) btnMute.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 3), defaultButtonBorder));
            else if (currentTutorialStep == 17) btnSwap.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 3), defaultButtonBorder));
            else if (currentTutorialStep == 18) btnNewGame.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 3), defaultButtonBorder));
            else if (currentTutorialStep == 19) btnMenu.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 3), defaultButtonBorder));

            if (currentTutorialStep == 11 || currentTutorialStep == 13 || currentTutorialStep == 15) {
                dialogText.setVisible(false);
                overlayPanel.setVisible(false);
            }
            
            if (currentTutorialStep == 10) {
                loadTutorialMatrix(1);
            } else if (currentTutorialStep == 12) {
                loadTutorialMatrix(2);
            } else if (currentTutorialStep == 14) {
                loadTutorialMatrix(3);
            }
        }
    }

    /**
     * Finishes the tutorial and presents the user with options to either
     * review the tutorial again or return to the main menu.
     */
    private void endTutorial() {
        clearHighlights();
        overlayPanel.setVisible(true);
        dialogText.setVisible(true);
        dialogText.setText("You've got the hang of it. Do you want to review the tutorial or return to the menu?");
        
        btnSkip.setVisible(false);
        btnNext.setVisible(false);
        btnReview.setVisible(true);
        btnReturnMenu.setVisible(true);
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
            // Continue chosen, do nothing since there's no timer
        } else if (choice == 1) {
            // Switch to Main Menu if "Main Menu" is chosen
            MenuUI menu = new MenuUI(parent);
            parent.switchPanel(menu);
        }
    }

    // Timer and GameOver logic removed since Tutorial doesn't need it

    /**
     * Starts the game by resetting necessary stats and generating the matrix.
     */
    private void startGame() {
        swapsLabel.setText("Swaps: 10");
        levelLabel.setText("Level: 0");
        currentPath = null;
        progress = 0;
        
        gamePanel.setLayout(new GridLayout(this.rows, this.cols, 0, 0));
        loadTutorialMatrix(0);
    }

    private void handleLevelComplete() {
        nextTutorialStep();
        gamePanel.setBorder(BorderFactory.createEmptyBorder());
    }
    
    /**
     * Get image from folder and mapping into an array
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
                if (currentImgID == -2) {
                    btn.setBackground(Color.DARK_GRAY);
                    btn.setOpaque(true);
                    btn.setEnabled(false);
                } else {
                    btn.setIcon(pieceIcons[currentImgID]);
                }
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
                    parent.playSound("sound2.wav");
                } // State 2: User clicks the exact same button again (Deselect)
                else if (firstSelected == currentClick) {
                    firstSelected.setBorder(BorderFactory.createEmptyBorder());
                    firstSelected = null;
                    parent.playSound("sound1.wav");
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

                            timer.setRepeats(false);
                            timer.start();

                            parent.playSound("sound5.wav");

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
                            parent.playSound("sound1.wav");
                        }
                    } // IDs do not match (Different images selected)
                    else {
                        if (score > 0) {
                            score -= 10; // Penalty
                        }
                        parent.playSound("sound1.wav");
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
     * Loads a specific predefined matrix layout for the tutorial.
     * These layouts are designed to demonstrate different connection types
     * (straight line, 1 turn, 2 turns).
     *
     * @param type The type of tutorial layout to load (1: straight, 2: 1 turn, 3: 2 turns).
     */
    private void loadTutorialMatrix(int type) {
        int[][] m = new int[rows][cols];
        for(int[] r : m) Arrays.fill(r, -1);
        
        if (type == 1) {
            for(int i=1; i<=3; i++) {
                for(int j=1; j<=5; j++) { m[i][j] = -2; m[i][j+8] = -2; }
            }
            m[1][7] = 0;
            m[5][1] = 1; m[5][13] = 1;
            for(int i=7; i<=9; i++) {
                for(int j=1; j<=5; j++) { m[i][j] = -2; m[i][j+8] = -2; }
            }
            m[9][7] = 0;
            numberOfImage = 4; // 2 pairs (4 pieces)
        } else if (type == 2) {
            for(int i=1; i<=3; i++) { for(int j=1; j<=5; j++) { m[i][j] = -2; m[i][j+8] = -2; } }
            m[1][6] = 0; m[1][8] = 1;
            m[4][1] = 0; m[4][13] = 1;
            m[6][1] = 2; m[6][13] = 3;
            for(int i=7; i<=9; i++) { for(int j=1; j<=5; j++) { m[i][j] = -2; m[i][j+8] = -2; } }
            m[9][6] = 2; m[9][8] = 3;
            numberOfImage = 8; // 4 pairs
        } else if (type == 3) {
            for(int i=1; i<=3; i++) {
                for(int j=1; j<=5; j++) { m[i][j] = -2; m[i][j+8] = -2; }
            }
            for(int i=7; i<=9; i++) {
                for(int j=1; j<=5; j++) { m[i][j] = -2; m[i][j+8] = -2; }
            }
            m[1][3] = 0; m[1][11] = 0;
            m[2][1] = 3; m[2][5] = 4; m[2][9] = 5; m[2][13] = 1;
            m[3][3] = 6; m[3][11] = 7;
            
            m[7][3] = 7; m[7][11] = 6;
            m[8][1] = 3; m[8][5] = 5; m[8][9] = 4; m[8][13] = 1;
            m[9][3] = 2; m[9][11] = 2;
            numberOfImage = 16; // 8 pairs
        }
        
        gameLogic = new GameLogic(m);
        progress = 0;
        level(gameLogic.getMatrix());
    }
}
