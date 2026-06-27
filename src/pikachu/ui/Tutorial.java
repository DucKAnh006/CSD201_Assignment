package pikachu.ui;

import javax.swing.*;
import java.awt.*;

public class Tutorial extends JPanel {

    private final MainFrame parent;

    public Tutorial(MainFrame parent) {
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        this.setLayout(new GridBagLayout());
        this.setBackground(new Color(255, 248, 220));

        JPanel tutorialPanel = new JPanel(new GridBagLayout());
        tutorialPanel.setBackground(new Color(255, 248, 220));
        tutorialPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 204, 0), 3),
                BorderFactory.createEmptyBorder(22, 30, 22, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("HOW TO PLAY", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(new Color(230, 126, 34));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 18, 0);
        tutorialPanel.add(title, gbc);

        addTutorialStep(tutorialPanel, gbc, 1, "Step 1: Choose a difficulty", "Click Start, then select Easy, Normal, or Hard.");
        addTutorialStep(tutorialPanel, gbc, 2, "Step 2: Find matching Pokemon", "Look for two tiles that have the same Pokemon image.");
        addTutorialStep(tutorialPanel, gbc, 3, "Step 3: Connect two tiles", "Click the first tile, then click the second matching tile.");
        addTutorialStep(tutorialPanel, gbc, 4, "Step 4: Follow the connection rule", "Two tiles can be removed only if the path has no more than 3 straight lines.");
        addTutorialStep(tutorialPanel, gbc, 5, "Step 5: Score points", "Each correct pair gives points. Wrong moves reduce your score.");
        addTutorialStep(tutorialPanel, gbc, 6, "Step 6: Watch the timer", "Clear all Pokemon before time runs out.");
        addTutorialStep(tutorialPanel, gbc, 7, "Step 7: Win the level", "Clear the board to complete the level and continue to the next one.");

        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(180, 50));
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setBackground(new Color(255, 204, 0));
        backButton.setForeground(Color.BLACK);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            parent.playSound("sound2.wav");
            parent.switchPanel(new MenuUI(parent));
        });

        gbc.gridy = 8;
        gbc.insets = new Insets(20, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        tutorialPanel.add(backButton, gbc);

        this.add(tutorialPanel);
    }

    private void addTutorialStep(JPanel panel, GridBagConstraints gbc, int row, String stepTitle, String description) {
        JPanel stepPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        stepPanel.setOpaque(false);

        JLabel title = new JLabel(stepTitle);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(52, 73, 94));

        JLabel detail = new JLabel(description);
        detail.setFont(new Font("Arial", Font.PLAIN, 15));
        detail.setForeground(Color.BLACK);

        stepPanel.add(title);
        stepPanel.add(detail);

        gbc.gridy = row;
        gbc.insets = new Insets(8, 0, 4, 0);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(stepPanel, gbc);
    }
}
