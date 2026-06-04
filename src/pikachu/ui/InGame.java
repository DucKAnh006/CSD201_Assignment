package pikachu.ui;

import pikachu.logic.GameLogic;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JOptionPane;

/*
Giac Flow trai phai (y)
Trong Logic swap findpath
Long logic create check_matrix
Duy ui thiet ke giao dien menu, thanh thgian, vẽ dường đi, diểm,....
Dung flow       tren xuong x
 */
public class InGame extends JPanel {

    private MainFrame parent;
    private JPanel gamePanel = new JPanel();
    private Dimension buttonFixedSize = new Dimension(40, 50);
    private GameLogic gameLogic;
    private int numberOfImage;
    private int progress;
    private int currentLevel = 1;
    private int maxLevel = 9;

    private int rows, cols;
    private int totalRows, totalCols;
    private PokemonButton firstSelected = null;

    public InGame(int x, int y, MainFrame parent) {
        this.parent = parent;
        this.rows = x;
        this.cols = y;
        this.numberOfImage = x * y;
        this.totalRows = x + 2;
        this.totalCols = y + 2;

        this.gameLogic = new GameLogic(x, y);
        gamePanel.setLayout(new GridLayout(x, y, 0, 0));
        initUI();
    }

    private void initUI() {
        this.setLayout(new GridBagLayout());
        startGame();
        this.add(gamePanel);
    }
    
    private void startGame() {
        gameLogic.createMatrix();
        progress = 0;
        level(gameLogic.getMatrix());
    } 

    private void level(int[][] matrix) {
        gamePanel.removeAll();
        firstSelected = null;

        ImageIcon[] pieceIcons = new ImageIcon[36];
        for (int i = 0; i < 36; i++) {
            String fileName = "pieces" + (i + 1) + ".png";
            java.net.URL imgURL = getClass().getResource("/pikachu/img/" + fileName);
            if (imgURL != null) {
                pieceIcons[i] = new ImageIcon(imgURL);
            }
        }

        int row = -1;
        for (int i = 0; i < rows * cols; i++) {
            int col = i % cols;
            if (col == 0) {
                row++;
            }

            int currentImgID = matrix[row + 1][col + 1];
            PokemonButton btn = new PokemonButton(row, col, currentImgID);
            if (currentImgID != -1) {
                btn.setIcon(pieceIcons[currentImgID]);
            } else {
                btn.setVisible(false);
            }

            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusPainted(false);
            btn.setPreferredSize(buttonFixedSize);
            btn.setMinimumSize(buttonFixedSize);
            btn.setMaximumSize(buttonFixedSize);

            gamePanel.add(btn);

            btn.addActionListener(e -> {
                PokemonButton currentClick = (PokemonButton) e.getSource();

                if (firstSelected == null) {
                    firstSelected = currentClick;
                    firstSelected.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.RED, 3));
                } else if (firstSelected == currentClick) {
                    firstSelected.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                    firstSelected = null;
                } else {
                    int id1 = firstSelected.getImageID();
                    int id2 = currentClick.getImageID();

                    if (id1 == id2) {
                        boolean canConnect = gameLogic.findPath(
                                firstSelected.getRow(), firstSelected.getCol(),
                                currentClick.getRow(), currentClick.getCol());
                        if (canConnect) {
                            gameLogic.updateMatrix(firstSelected.getRow() + 1, firstSelected.getCol() + 1, currentClick.getRow() + 1, currentClick.getCol() + 1, currentLevel);
                            firstSelected.setVisible(false);
                            currentClick.setVisible(false);
                            progress += 2;
                            if (!gameLogic.checkValidMatrix()) {
                                level(gameLogic.swapMatrix());
                            }

                            if (progress >= numberOfImage) {
                                handleLevelComplete();
                            }
                        } else {
                            firstSelected.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                        }
                    } else {
                        firstSelected.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                    }
                    firstSelected = null;
                }
            });
        }

        gamePanel.revalidate();
        gamePanel.repaint();
    }

    private void handleLevelComplete() {
        currentLevel++;

        if (currentLevel <= maxLevel) {
            JOptionPane.showMessageDialog(parent, "Hoàn thành màn chơi! Chuẩn bị sang Level " + currentLevel);
            startGame();
        } else {
            JOptionPane.showMessageDialog(parent, "Chúc mừng bạn đã phá đảo toàn bộ trò chơi!");
            MenuUI menu = new MenuUI(parent);
            parent.switchPanel(menu);
        }
    }
}
