package pikachu.ui;

import pikachu.logic.GameLogic;
import pikachu.logic.PokemonNode;

import javax.swing.*;
import java.awt.*;
import pikachu.data.Achievement;
import pikachu.data.AchievementManager;

/**
 * The InGame class represents the main game panel where the puzzle is displayed and played.
 */
public class InGame extends JPanel {

    private MainFrame parent; 
    private JPanel gamePanel = new JPanel(); 
    private GameLogic gameLogic; 
    private int numberOfImage; 
    private int progress; 
    private int currentLevel = 1; 
    private int maxLevel = 9; 

    private int rows, cols;
    private String difficulty;
    private PokemonNode firstSelected = null; 

    private int score = 0; 
    private int swapsAvailable = 3; 
    private boolean isMuted = false;

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
     * @param x      The number of rows in the matrix.
     * @param y      The number of columns in the matrix.
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

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(1100, 900));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        timeBar = new JProgressBar(0, MAX_TIME);
        timeBar.setValue(MAX_TIME);
        timeBar.setPreferredSize(new Dimension(1260, 25));
        timeBar.setStringPainted(true);
        timeBar.setForeground(Color.GREEN);
        rightPanel.add(timeBar, BorderLayout.NORTH);

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
            InGame newGame = new InGame(rows, cols, difficulty, parent);
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
            if (timeLeft <= (MAX_TIME - 2 * MAX_TIME/10)  && timeLeft > (MAX_TIME - 7 * MAX_TIME/10)) {
                timeBar.setForeground(Color.YELLOW);
            } else if (timeLeft <= (MAX_TIME - 7 * MAX_TIME/10)) {
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
        swapsAvailable = 3;
        swapsLabel.setText("Swaps: " + swapsAvailable);
        levelLabel.setText("Level: " + currentLevel);
        
        gameLogic.createMatrix();
        progress = 0;
        level(gameLogic.getMatrix());
        playSound("sound4.wav");
        
        setupTimer();
    }

    /**
     * Sets up the game interface for the current level based on the provided matrix.
     *
     * @param matrix The 2D array representing the game state.
     */
    private void level(int[][] matrix) {
        gamePanel.removeAll();
        firstSelected = null;
 
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
            PokemonNode btn = new PokemonNode( row, col, currentImgID);
            
            // Verify if the current image ID is valid to show the icon, else hide the button
            if (currentImgID != -1) { 
                btn.setIcon(pieceIcons[currentImgID]);
            } else { 
                btn.setVisible(true);
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
                } 
                // If the user clicks the currently selected button again
                else if (firstSelected == currentClick) { 
                    firstSelected.setBorder(BorderFactory.createEmptyBorder()); 
                    firstSelected = null; 
                    playSound("sound1.wav"); 
                } 
                // If a second different button is clicked
                else {
                    int id1 = firstSelected.getImageID();
                    int id2 = currentClick.getImageID();

                    // Check if both selected buttons have the same image ID
                    if (id1 == id2) { 
                        boolean canConnect = gameLogic.findPath(firstSelected, currentClick);

                        // If a valid path exists between the two nodes
                        if (canConnect) { 
                            // Add 20 points for a successful connection
                            score += 20;
                            
                            PokemonNode node1 = firstSelected;
                            PokemonNode node2 = currentClick;
                            
                            Timer timer = new Timer(150, t -> {
                                level(gameLogic.updateMatrix(node1.getRow(), node1.getCol(), node2.getRow(), node2.getCol(), currentLevel));
                            });
                            
                            timer.setRepeats(false);
                            timer.start();
                            
                            playSound("sound5.wav");
                            // Increment progress by 2 since a pair is cleared
                            progress += 2; 

                            // If there are no valid moves left on the board, force a swap
                            if (!gameLogic.checkValidMatrix()) { 
                                level(gameLogic.swapMatrix());
                                playSound("sound4.wav");
                            }

                            // If the board is fully cleared
                            if (progress >= numberOfImage) { 
                                handleLevelComplete();
                            }
                        } 
                        // If path is invalid
                        else {
                            if (score > 0) {
                                score -= 10;
                            }
                            firstSelected.setBorder(BorderFactory.createEmptyBorder());
                            playSound("sound1.wav");
                        }
                    } 
                    // If the IDs do not match
                    else {
                        if (score > 0) {
                            score -= 10;
                        }
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
        } 
        // If all levels are completed
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
        if (isMuted) return;

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