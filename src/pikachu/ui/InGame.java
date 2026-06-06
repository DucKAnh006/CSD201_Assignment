package pikachu.ui;

import pikachu.logic.GameLogic;
import pikachu.logic.PokemonNode;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JOptionPane;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * The InGame class represents the main game panel where the puzzle is displayed and played.
 */
public class InGame extends JPanel {

    private MainFrame parent; // Reference to the main frame to allow switching panels and accessing shared resources
    private JPanel gamePanel = new JPanel(); // Panel to hold the grid of buttons (images)
    private Dimension buttonFixedSize = new Dimension(40, 50); // Fixed size for each button (image) in the grid
    private GameLogic gameLogic; // Game logic handler for managing the game state and rules
    private int numberOfImage; // Total number of images (buttons) in the current level
    private int progress; // Track the number of matched pairs
    private int currentLevel = 1; // Current level of the game
    private int maxLevel = 9; // Maximum level of the game

    private int rows, cols;
    private PokemonNode firstSelected = null; // Reference to the first selected button (image) for matching

    public InGame(int x, int y, MainFrame parent) {
        this.parent = parent;
        this.rows = x;
        this.cols = y;
        this.numberOfImage = x * y;

        this.gameLogic = new GameLogic(x, y); // Initialize game logic with the specified grid size
        gamePanel.setLayout(new GridLayout(x, y, 0, 0)); // Set the layout of the game panel to a grid with no gaps
        initUI();
    }


    /**
     * Initializes the user interface for the game panel.
     */
    private void initUI() {
        this.setLayout(new GridBagLayout());
        startGame();
        this.add(gamePanel);
    }

    /**
     * Starts the game by creating the initial matrix and setting up the level.
     */
    private void startGame() {
        gameLogic.createMatrix();
        progress = 0;
        level(gameLogic.getMatrix());
        playSound("sound4.wav");
    }

    /**
     * Sets up the game for a new level.
     * Mapping image IDs to buttons and adding action listeners for user interactions.
     * @param matrix The matrix representing the current game state.
     */
    private void level(int[][] matrix) {
        gamePanel.removeAll();
        firstSelected = null;
 
        // Load all piece icons into an array for easy access
        ImageIcon[] pieceIcons = new ImageIcon[36];
        for (int i = 0; i < 36; i++) {
            String fileName = "pieces" + (i + 1) + ".png";
            java.net.URL imgURL = getClass().getResource("/pikachu/img/" + fileName);
            if (imgURL != null) {
                pieceIcons[i] = new ImageIcon(imgURL);
            }
        }

        // Create buttons for each cell in the grid based on the matrix and add action listeners
        int row = -1;
        for (int i = 0; i < rows * cols; i++) {
            int col = i % cols;
            if (col == 0) {
                row++;
            }

            // Get the image ID for the current cell from the matrix and create a button for it
            int currentImgID = matrix[row + 1][col + 1];
            PokemonNode btn = new PokemonNode(row, col, currentImgID);
            if (currentImgID != -1) { // If the image ID is valid, set the corresponding icon for the button
                btn.setIcon(pieceIcons[currentImgID]);
            } else { // If the image ID is -1, it means the cell is empty, so we hide the button
                btn.setVisible(false);
            }

            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusPainted(false);
            btn.setPreferredSize(buttonFixedSize);
            btn.setMinimumSize(buttonFixedSize);
            btn.setMaximumSize(buttonFixedSize);

            gamePanel.add(btn);

            btn.addActionListener(e -> { // Action listener for when a button (image) is clicked
                PokemonNode currentClick = (PokemonNode) e.getSource();

                if (firstSelected == null) { // If no button is currently selected, set the clicked button as the first selected and highlight it
                    firstSelected = currentClick; // Set the first selected button to the currently clicked button
                    firstSelected.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.RED, 3)); // Highlight the first selected button with a red border
                    playSound("sound2.wav"); // Play a sound to indicate that a button has been selected
                } else if (firstSelected == currentClick) { // If the same button is clicked again, deselect it and remove the highlight
                    firstSelected.setBorder(javax.swing.BorderFactory.createEmptyBorder()); // Remove the border to indicate that the button is no longer selected
                    firstSelected = null; // Reset the first selected button to null
                    playSound("sound1.wav"); // Play a sound to indicate that the selection has been canceled
                } else {
                    // If a different button is clicked, check if the two selected buttons have the same image ID
                    int id1 = firstSelected.getImageID();
                    int id2 = currentClick.getImageID();

                    if (id1 == id2) { // If the image IDs match, check if the two buttons can be connected according to the game rules
                        boolean canConnect = gameLogic.findPath(firstSelected, currentClick);

                        if (canConnect) { // If the buttons can be connected, update the game state to reflect the matched pair and check for level completion
                            level(gameLogic.updateMatrix(firstSelected.getRow() + 1, firstSelected.getCol() + 1, currentClick.getRow() + 1, currentClick.getCol() + 1, currentLevel));
                            playSound("sound5.wav");
                            progress += 2; // Increment the progress by 2 for each matched pair

                            if (!gameLogic.checkValidMatrix()) { // If the current matrix is not valid (no more moves available), shuffle the remaining pieces and update the level
                                level(gameLogic.swapMatrix());
                                playSound("sound4.wav");
                            }

                            if (progress >= numberOfImage) { // If all pairs have been matched, handle level completion
                                handleLevelComplete();
                            }
                        } else { // If the buttons cannot be connected, deselect the first selected button and play a sound to indicate an invalid match
                            firstSelected.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                        }
                    } else { // If the image IDs do not match, deselect the first selected button and play a sound to indicate an invalid match
                        firstSelected.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                        playSound("sound1.wav");
                    }
                    firstSelected = null;
                }
            });
        }

        // Refresh the game panel to reflect the updated buttons and their states
        gamePanel.revalidate();
        gamePanel.repaint();
    }


    /**
     * Handles the completion of a level by checking if the current level is less than or equal to the maximum level.
     * If there are more levels to play, it displays a congratulatory message and starts the next level. If the player has completed all levels, it displays a final congratulatory message and returns to the main menu.
     */
    private void handleLevelComplete() {
        currentLevel++; // Increment the current level to move on to the next level

        if (currentLevel <= maxLevel) { // If there are more levels to play, display a congratulatory message and start the next level
            JOptionPane.showMessageDialog(parent, "Congratulations! You finished level " + currentLevel); // Display a message dialog to congratulate the player for completing the current level
            startGame(); // Start the next level by resetting the game state and updating the UI
        } else { // If the player has completed all levels, display a final congratulatory message and return to the main menu
            JOptionPane.showMessageDialog(parent, "Congratulations you finished the game!"); // Display a message dialog to congratulate the player for completing the entire game
            MenuUI menu = new MenuUI(parent); // Create a new instance of the MenuUI class to represent the main menu panel
            parent.switchPanel(menu); // Switch the current panel in the main frame to the main menu panel, allowing the player to start a new game or exit
        }
    }

    /**
     * Plays a sound effect based on the provided sound file name.
     * @param soundFileName The name of the sound file to be played (e.g., "sound1.wav").
     */
    public void playSound(String soundFileName) {
        // Construct the path to the sound file and attempt to load and play it
        try {
            String soundPath = "/pikachu/sound/" + soundFileName;
            java.net.URL soundURL = InGame.class.getResource(soundPath);

            if (soundURL != null) { // If the sound file is found, load it as an audio input stream and play it using a Clip
                javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(soundURL);
                javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } else { // If the sound file is not found, print an error message to the console
                System.out.println("Error: Can not find sound with path: " + soundFileName);
            }
        } catch (Exception e) { // If there is an error while loading or playing the sound, print an error message to the console
            System.out.println("Can not play sound: " + e.getMessage());
        }
    }
}
