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
        this.add(createMenuButton("How to play"), gbc);

        gbc.gridy = 3;
        this.add(createMenuButton("About us"), gbc);

        gbc.gridy = 4;
        this.add(createMenuButton("Achievement"), gbc);

        gbc.gridy = 5;
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
        button.addActionListener(e -> {
            parent.playSound("sound2.wav");
            handleMenuAction(text);
        }); // Delegate click handling
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
            case "How to play" -> parent.switchPanel(new Tutorial(this.parent));
            case "About us" -> showAboutUs();
            case "Achievement" -> showAchievements();
            case "Exit" -> {
                JOptionPane.showMessageDialog(parent, "Goodbye and see you again!", "Quit", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        }
    }

    private void showHowToPlay() {
        StringBuilder builder = new StringBuilder();
        builder.append("How to play Pikachu Classic:\n\n");
        builder.append("Step 1: Click Start and choose a difficulty level.\n");
        builder.append("Step 2: Find two Pokemon tiles with the same image.\n");
        builder.append("Step 3: Select both tiles to connect them.\n");
        builder.append("Step 4: A pair is removed only when the path has no more than 3 straight lines.\n");
        builder.append("Step 5: Clear all tiles before the time runs out.\n");
        builder.append("Step 6: Use Swap when there are no easy moves left.\n");
        builder.append("Step 7: Complete the board to win and continue to the next level.");

        JTextArea display = new JTextArea(builder.toString());
        display.setEditable(false);
        display.setBackground(this.getBackground());
        display.setFont(new Font("Arial", Font.PLAIN, 16));
        display.setBorder(null);
        display.setLineWrap(true);
        display.setWrapStyleWord(true);

        JOptionPane.showMessageDialog(parent, new JScrollPane(display), "How to play", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutUs() {
        JPanel aboutPanel = new JPanel(new GridBagLayout());
        aboutPanel.setBackground(new Color(255, 248, 220));
        aboutPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 204, 0), 3),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("PIKACHU CLASSIC - ABOUT US", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(230, 126, 34));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        aboutPanel.add(title, gbc);

        addAboutSection(aboutPanel, gbc, 1, "Course Information");
        addAboutLine(aboutPanel, gbc, 2, "School: FPT University");
        addAboutLine(aboutPanel, gbc, 3, "Class: SE");
        addAboutLine(aboutPanel, gbc, 4, "Semester: Summer 2026");
        addAboutLine(aboutPanel, gbc, 5, "Subject: CSD201");

        addAboutSection(aboutPanel, gbc, 6, "Team Members");
        addAboutLine(aboutPanel, gbc, 7, "1. CE200031 - Nguyễn Trần Đức Anh");
        addAboutLine(aboutPanel, gbc, 8, "2. CE200291 - Trần Huỳnh Giác");
        addAboutLine(aboutPanel, gbc, 9, "3. CE200340 - Nguyễn Phú Trọng");
        addAboutLine(aboutPanel, gbc, 10, "4. CE201046 - Lê Tiến Dũng");
        addAboutLine(aboutPanel, gbc,11, "5. CE201224 - Huỳnh Nhật Duy");
        addAboutLine(aboutPanel, gbc, 12, "6. CE181332 - Nguyễn Lâm Hoàng Long");


        addAboutSection(aboutPanel, gbc, 13, "Mentor");
        addAboutLine(aboutPanel, gbc, 14, "Mentor LanLTT");

        JOptionPane.showMessageDialog(parent, aboutPanel, "About us", JOptionPane.PLAIN_MESSAGE);
    }

    private void addAboutSection(JPanel panel, GridBagConstraints gbc, int row, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(new Color(52, 73, 94));
        gbc.gridy = row;
        gbc.insets = new Insets(row == 1 ? 0 : 14, 0, 6, 0);
        panel.add(label, gbc);
    }

    private void addAboutLine(JPanel panel, GridBagConstraints gbc, int row, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 15));
        label.setForeground(Color.BLACK);
        gbc.gridy = row;
        gbc.insets = new Insets(2, 14, 2, 0);
        panel.add(label, gbc);
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
     * Uses AchievementManager.getTopAchievements(5, difficulty).
     */
    private void showAchievements() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 15));
        tabbedPane.setBackground(new Color(255, 248, 220));

        String[] difficulties = {"Easy", "Normal", "Hard"};
        for (String difficulty : difficulties) {
            JPanel achievementPanel = new JPanel(new GridBagLayout());
            achievementPanel.setBackground(new Color(255, 248, 220));
            achievementPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 204, 0), 3),
                    BorderFactory.createEmptyBorder(18, 24, 18, 24)
            ));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            JLabel title = new JLabel("TOP 5 HIGH SCORES - " + difficulty.toUpperCase(), SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 22));
            title.setForeground(new Color(230, 126, 34));
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 16, 0);
            achievementPanel.add(title, gbc);

            List<Achievement> top = achievementManager.getTopAchievements(5, difficulty);
            if (top.isEmpty()) {
                JLabel emptyLabel = new JLabel("No achievements yet for " + difficulty + ".");
                emptyLabel.setFont(new Font("Arial", Font.PLAIN, 15));
                emptyLabel.setForeground(Color.BLACK);
                gbc.gridy = 1;
                gbc.insets = new Insets(2, 14, 2, 0);
                achievementPanel.add(emptyLabel, gbc);
            } else {
                int rank = 1;
                for (Achievement achievement : top) {
                    JLabel scoreLabel = new JLabel(rank + ". " + achievement.getScore() + " points");
                    scoreLabel.setFont(new Font("Arial", Font.PLAIN, 15));
                    scoreLabel.setForeground(Color.BLACK);
                    gbc.gridy = rank;
                    gbc.insets = new Insets(2, 14, 2, 0);
                    achievementPanel.add(scoreLabel, gbc);
                    rank++;
                }
            }

            tabbedPane.addTab(difficulty, achievementPanel);
        }

        JOptionPane.showMessageDialog(parent, tabbedPane, "Achievements", JOptionPane.PLAIN_MESSAGE);
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
