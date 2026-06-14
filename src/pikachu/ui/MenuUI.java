/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pikachu.ui;

import pikachu.data.Achievement;
import pikachu.data.AchievementManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * MenuUI - main menu panel for the Pikachu game.
 *
 * Responsibilities:
 * - Render the main menu UI and background image.
 * - Provide actions to start a new game, view achievements, or exit.
 * - Bridge user choices to InGame and AchievementManager APIs.
 *
 * Usage notes:
 * - To start a game programmatically call:
 *     parent.switchPanel(new InGame(rows, cols, difficulty, parent));
 * - To retrieve top scores programmatically call:
 *     new AchievementManager().getTopAchievements(max, difficulty);
 */
public class MenuUI extends JPanel {

    // Reference to the main application frame used for panel switching and dialogs.
    private final MainFrame parent;
    // Optional background image for the menu.
    private Image backgroundImage;
    // Single AchievementManager instance for querying stored scores.
    private final AchievementManager achievementManager = new AchievementManager();

    /**
     * Construct the menu UI.
     *
     * @param parent main application frame used to switch panels and show dialogs
     */
    public MenuUI(MainFrame parent) {
        this.parent = parent;
        loadBackgroundImage(); // Load background resource if available
        initUI();              // Build UI components
    }

    /**
     * Try to load background image from resources.
     * First attempts /pikachu/img then a fallback /img path.
     */
    private void loadBackgroundImage() {
        java.net.URL bgURL = getClass().getResource("/pikachu/img/bg_menu_pikachu.jpg");
        if (bgURL == null) {
            bgURL = getClass().getResource("/img/bg_menu_pikachu.jpg");
        }
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        }
    }

    /**
     * Initialize layout and add menu buttons.
     * Buttons call handleMenuAction when clicked.
     */
    private void initUI() {
        this.setLayout(new GridBagLayout());
        this.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(12, 12, 12, 12);

        JLabel title = new JLabel("PIKACHU CLASSIC");
        title.setFont(new Font("Arial", Font.BOLD, 52));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0;
        this.add(title, gbc);

        gbc.gridy = 1;
        this.add(createMenuButton("Start"), gbc);

        gbc.gridy = 2;
        this.add(createMenuButton("Achievement"), gbc);

        gbc.gridy = 3;
        this.add(createMenuButton("Exit"), gbc);
    }

    /**
     * Create a styled menu button with an action handler.
     *
     * @param text button label
     * @return configured JButton
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(280, 70));
        button.setFont(new Font("Arial", Font.BOLD, 22));
        button.setBackground(new Color(255, 204, 0));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.addActionListener(e -> handleMenuAction(text)); // Delegate click handling
        return button;
    }

    /**
     * Handle top-level menu actions.
     * "Start" -> show difficulty dialog and start game
     * "Achievement" -> show achievements dialog
     * "Exit" -> exit application
     *
     * @param action label of clicked button
     */
    private void handleMenuAction(String action) {
        switch (action) {
            case "Start" -> openDifficultyDialog();
            case "Achievement" -> showAchievements();
            case "Exit" -> System.exit(0);
        }
    }

    /**
     * Show a dialog to choose difficulty and then switch to InGame panel.
     * Mapping:
     * - Easy -> new InGame(8, 12, "Easy", parent)
     * - Normal -> new InGame(10, 15, "Normal", parent)
     * - Hard -> new InGame(12, 18, "Hard", parent)
     */
    private void openDifficultyDialog() {
        String[] options = {"Easy", "Normal", "Hard", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                "Select difficulty level",
                "Start Game",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[1]
        );

        if (choice == 0) {
            parent.switchPanel(new InGame(8, 12, "Easy", parent));
        } else if (choice == 1) {
            parent.switchPanel(new InGame(10, 15, "Normal", parent));
        } else if (choice == 2) {
            parent.switchPanel(new InGame(12, 18, "Hard", parent));
        }
    }

    /**
     * Show a dialog to choose difficulty and then display top achievements for that difficulty.
     * Uses AchievementManager.getTopAchievements(8, selectedDifficulty).
     */
    private void showAchievements() {
        String[] options = {"Easy", "Normal", "Hard", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                "Select difficulty to view top scores",
                "Achievements - Select Difficulty",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[1]
        );

        if (choice < 0 || choice >= 3) return;
        String selectedDifficulty = options[choice];

        // Query top 8 scores for the chosen difficulty
        List<Achievement> top = achievementManager.getTopAchievements(8, selectedDifficulty);
        if (top.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No achievements yet for " + selectedDifficulty + ".", "Top Scores", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Build a simple textual representation of the top scores
        StringBuilder builder = new StringBuilder();
        builder.append("Top 8 High Scores (").append(selectedDifficulty).append("):\n\n");
        int rank = 1;
        for (Achievement achievement : top) {
            builder.append(rank)
                    .append(". ")
                    .append(achievement.getScore())
                    .append(" points - ")
                    .append(achievement.getDifficulty())
                    .append("\n");
            rank++;
        }

        JTextArea display = new JTextArea(builder.toString());
        display.setEditable(false);
        display.setBackground(this.getBackground());
        display.setFont(new Font("Arial", Font.PLAIN, 16));
        display.setBorder(null);

        JOptionPane.showMessageDialog(parent, new JScrollPane(display), "Achievements", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Paint background image if available.
     *
     * @param g Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
