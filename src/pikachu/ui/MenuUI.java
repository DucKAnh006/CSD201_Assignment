package pikachu.ui;

import pikachu.io.AchievementManager;
import pikachu.io.AchievementManager.ScoreEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author ADMIN
 */
public class MenuUI extends JPanel {

    MainFrame parent;
    private Image backgroundImage;
    private AchievementManager achievementManager;

    public MenuUI(MainFrame parent) {
        this.parent = parent;
        achievementManager = new AchievementManager();
        loadBackgroundImage();
        initUI();
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = new ImageIcon("src/pikachu/img/bg_menu_pikachu.jpg").getImage();
        } catch (Exception e) {
            System.out.println("Không tìm thấy hình nền");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initUI() {
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(0, 0, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Pikachu Game");
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(new Color(255, 220, 0));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(Box.createVerticalStrut(40));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(40));

        add(titlePanel);
        add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(0, 0, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnStart = createButton("Start");
        btnStart.addActionListener(this::btnStartActionPerformed);
        buttonPanel.add(btnStart);
        buttonPanel.add(Box.createVerticalStrut(20));

        btnOption = createButton("Achievement");
        btnOption.addActionListener(this::btnOptionActionPerformed);
        buttonPanel.add(btnOption);
        buttonPanel.add(Box.createVerticalStrut(20));

        btnExit = createButton("Exit");
        btnExit.addActionListener(this::btnExitActionPerformed);
        buttonPanel.add(btnExit);

        add(buttonPanel);
        add(Box.createVerticalGlue());
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setPreferredSize(new Dimension(220, 55));
        btn.setMaximumSize(new Dimension(220, 55));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(66, 135, 245));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(50, 120, 220));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(66, 135, 245));
            }
        });

        return btn;
    }

    private void btnExitActionPerformed(ActionEvent evt) {
        System.exit(0);
    }

    private void btnOptionActionPerformed(ActionEvent evt) {
        List<ScoreEntry> topScores = achievementManager.loadTopScores(5);

        JDialog achievementDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Achievement", true);
        achievementDialog.setSize(420, 320);
        achievementDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 245, 245));

        JLabel label = new JLabel("Top 5 điểm cao nhất");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(15));

        if (topScores.isEmpty()) {
            JLabel empty = new JLabel("Chưa có điểm nào được lưu");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(empty);
        } else {
            int rank = 1;
            for (ScoreEntry entry : topScores) {
                JLabel item = new JLabel(rank + ". " + entry.getScore() + " điểm - " + entry.getDifficulty());
                item.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                item.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(item);
                panel.add(Box.createVerticalStrut(8));
                rank++;
            }
        }

        panel.add(Box.createVerticalGlue());
        JButton closeBtn = createButton("Đóng");
        closeBtn.setMaximumSize(new Dimension(120, 45));
        closeBtn.addActionListener(e -> achievementDialog.dispose());
        panel.add(closeBtn);

        achievementDialog.add(panel);
        achievementDialog.setVisible(true);
    }

    private void btnStartActionPerformed(ActionEvent evt) {
        showDifficultyDialog();
    }

    private void showDifficultyDialog() {
        JDialog difficultyDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chọn độ khó", true);
        difficultyDialog.setSize(380, 300);
        difficultyDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(245, 245, 245));

        JLabel label = new JLabel("Chọn độ khó để bắt đầu");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(20));

        JButton easyBtn = createButton("Easy");
        easyBtn.setMaximumSize(new Dimension(220, 55));
        easyBtn.addActionListener(e -> {
            difficultyDialog.dispose();
            startGame(1, "Easy");
        });
        panel.add(easyBtn);
        panel.add(Box.createVerticalStrut(12));

        JButton normalBtn = createButton("Normal");
        normalBtn.setMaximumSize(new Dimension(220, 55));
        normalBtn.addActionListener(e -> {
            difficultyDialog.dispose();
            startGame(2, "Normal");
        });
        panel.add(normalBtn);
        panel.add(Box.createVerticalStrut(12));

        JButton difficultBtn = createButton("Difficult");
        difficultBtn.setMaximumSize(new Dimension(220, 55));
        difficultBtn.addActionListener(e -> {
            difficultyDialog.dispose();
            startGame(3, "Difficult");
        });
        panel.add(difficultBtn);
        panel.add(Box.createVerticalGlue());

        difficultyDialog.add(panel);
        difficultyDialog.setVisible(true);
    }

    private void startGame(int difficulty, String difficultyLabel) {
        InGame game = createGameWithDifficulty(difficulty);
        parent.switchPanel(game);
        // Nếu cần lưu điểm cuối game, gọi achievementManager.saveScore(score, difficultyLabel)
    }

    private InGame createGameWithDifficulty(int difficulty) {
        try {
            Constructor<InGame> ctor = InGame.class.getConstructor(int.class, int.class, int.class, MainFrame.class);
            return ctor.newInstance(10, 15, difficulty, parent);
        } catch (Exception e) {
            return new InGame(10, 15, parent);
        }
    }

    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnOption;
    private javax.swing.JButton btnStart;
}