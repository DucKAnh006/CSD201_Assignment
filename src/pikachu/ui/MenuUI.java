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

public class MenuUI extends JPanel {

    private final MainFrame parent;
    private Image backgroundImage;
    private final AchievementManager achievementManager = new AchievementManager();

    public MenuUI(MainFrame parent) {
        this.parent = parent;
        loadBackgroundImage();
        initUI();
    }

    private void loadBackgroundImage() {
        java.net.URL bgURL = getClass().getResource("/pikachu/img/bg_menu_pikachu.jpg");
        if (bgURL == null) {
            bgURL = getClass().getResource("/img/bg_menu_pikachu.jpg");
        }
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        }
    }

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

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(280, 70));
        button.setFont(new Font("Arial", Font.BOLD, 22));
        button.setBackground(new Color(255, 204, 0));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.addActionListener(e -> handleMenuAction(text));
        return button;
    }

    private void handleMenuAction(String action) {
        switch (action) {
            case "Start" -> openDifficultyDialog();
            case "Achievement" -> showAchievements();
            case "Exit" -> System.exit(0);
        }
    }

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
            parent.switchPanel(new InGame(8, 12, parent));
        } else if (choice == 1) {
            parent.switchPanel(new InGame(10, 15, parent));
        } else if (choice == 2) {
            parent.switchPanel(new InGame(12, 18, parent));
        }
    }

    private void showAchievements() {
        List<Achievement> top = achievementManager.getTopAchievements(8);
        if (top.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No achievements yet.", "Top Scores", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Top 8 High Scores:\n\n");
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
