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

    // --- Tutorial UI Components ---
    private JPanel overlayPanel;
    private JTextArea dialogText;
    private JButton btnSkip;
    private JButton btnNext;
    private JButton btnReview;
    private JButton btnReturnMenu;
    private int currentTutorialStep = 0;
    private String[] tutorialSteps = {
        "Chào bạn! Chào mừng đến với Pikachu Classic. Để mình hướng dẫn bạn cách chơi nhé!",
        "Ở góc bên trái. Đầu tiên là thông tin Level.",
        "Thứ hai là điểm số của bạn trong quá trình chơi (Score).",
        "Và cuối cùng số lần bạn được phép đổi mới bảng (Swaps).",
        "Mỗi lần bạn nhấn làm mới bảng hay trò chơi phát hiện không còn đường đi khả dụng thì vị trí các hình ảnh sẽ được thay đổi và số lần khả dụng sẽ giảm.",
        "Nhưng bạn yên tâm vì sau mỗi bàn bạn sẽ được tặng một lần làm mới bảng.",
        "Thanh màu xanh phía trên là thời gian. Bạn phải hoàn thành màn chơi trước khi hết giờ!",
        "Phần ở giữa là bảng game. Hãy tìm 2 hình giống nhau và click vào chúng để kết nối.",
        "Bạn có thể nối 2 hình nếu đường nối giữa chúng có tối đa 2 lần rẽ (3 đoạn thẳng).",
        "Cùng thử nhé",
        "Đầu tiên là những hình ảnh trên cùng một đường thẳng. Hãy click thử vào cặp hình nhé!",
        "",
        "Tiếp theo là các hình ảnh có thể kết nối bằng 1 lần rẽ. Bạn hãy tìm và nối chúng.",
        "",
        "Cuối cùng là các hình ảnh có thể kết nối bằng 2 lần rẽ (tối đa 3 đoạn thẳng).",
        "",
        "Bên dưới là các nút: Tắt âm (Mute)",
        "Đổi bảng (Swap)",
        "Màn chơi mới (New Game)",
        "Cuối cùng là Menu.",
        "Chúc bạn chơi game vui vẻ nhé!"
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

        getImage();
        initUI();
    }

    /**
     * Initializes the user interface for the game panel, dividing it into
     * sections.
     */
    private void initUI() {
        this.setLayout(new BorderLayout());
        
        JLayeredPane layeredPane = new JLayeredPane();
        
        // --- Base Game Panel ---
        JPanel basePanel = new JPanel(new BorderLayout());

        // --- Left Panel: Displays player stats (Level, Score, Swaps) ---
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
        basePanel.add(leftPanel, BorderLayout.WEST);

        // --- Right Panel: Contains the Time Bar, Game Board, and Action Buttons ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(1100, 900));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Time Bar setup at the top of the right panel
        timeBar = new JProgressBar(0, MAX_TIME);
        timeBar.setValue(MAX_TIME);
        timeBar.setPreferredSize(new Dimension(1260, 25));
        timeBar.setStringPainted(true);
        timeBar.setForeground(Color.GREEN);
        rightPanel.add(timeBar, BorderLayout.NORTH);

        // Wrapper panel to keep the game board centered
        JPanel gridWrapper = new JPanel(new GridBagLayout());
        gridWrapper.add(gamePanel);
        rightPanel.add(gridWrapper, BorderLayout.CENTER);

        // --- Bottom Panel: Action buttons (Mute, Swap, New Game, Menu) ---
        JPanel bottomButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));

        btnMute = new JButton(isMuted ? "Unmute" : "Mute");
        defaultButtonBorder = btnMute.getBorder();
        
        btnSwap = new JButton("Swap Matrix");
        btnNewGame = new JButton("New Game");
        btnMenu = new JButton("Menu");

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
            }
        });

        this.add(layeredPane, BorderLayout.CENTER);

        // Initialize level data and start the gameplay loop
        startGame();
    }
    
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

    private void endTutorial() {
        clearHighlights();
        overlayPanel.setVisible(true);
        dialogText.setVisible(true);
        dialogText.setText("Bạn đã nắm được cách chơi rồi. Bạn muốn xem lại hướng dẫn hay về menu?");
        
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

    /**
     * Plays a sound file given its file name.
     *
     * @param soundFileName The name of the sound file to be played.
     */
    public void playSound(String soundFileName) {
        if (isMuted) {
            return; // Skip sound playing if the game is muted
        }

        try {
            String soundPath = "/pikachu/sound/" + soundFileName;
            java.net.URL soundURL = InGame.class.getResource(soundPath);

            if (soundURL != null) {
                javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(soundURL);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                Timer timer = new Timer(3000, t -> {
                    clip.close();
                });

                timer.setRepeats(false);
                timer.start();
            } else {
                System.out.println("Error: Can not find sound with path: " + soundFileName);
            }
        } catch (Exception e) {
            System.out.println("Can not play sound: " + e.getMessage());
        }
    }
}
