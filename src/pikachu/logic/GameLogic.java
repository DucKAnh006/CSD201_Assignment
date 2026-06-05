/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pikachu.logic;

import pikachu.flow.Flows;

/**
 *
 * @author ADMIN
 */
public class GameLogic {

    private int rows;
    private int cols;
    private int[][] matrix;
    private Flows gameFlows = new Flows();

    public GameLogic(int x, int y) {
        this.rows = x;
        this.cols = y;
        this.matrix = new int[x + 2][y + 2];
    }

    /**
     * Long
     */
    public void createMatrix() {
        /*
    tạo ma trận với định dạng
    xxxxxxxxx
    x0000000x
    x0000000x
    xxxxxxxxx
    0: là phần hình ảnh khả dụng
    x: là phần dư của ma trận để tìm đường
         */
        for (int i = 0; i < rows + 2; i++) {
            for (int j = 0; j < cols + 2; j++) {
                if (i == 0 || i == rows + 1 || j == 0 || j == cols + 1) {
                    matrix[i][j] = -1;
                } else {
                    matrix[i][j] = 7; // id hinh ramdom tu 0-35
                }
            }
        }
    }

    /**
     * Long
     */
    // check conf duong naof kha dung trong matrix ko
    public boolean checkValidMatrix() {
        return true;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    /**
     * Trong
     */
    public int[][] swapMatrix() {
        for (int i = 0; i < rows + 2; i++) {
            for (int j = 0; j < cols + 2; j++) {
                if (matrix[i][j] != -1) {
                    matrix[i][j] = 4;
                }
            }
        }
        return matrix;
    }

    /**
     * Trong
     */
    public boolean findPath(PokemonNode pokemon1, PokemonNode pokemon2) {
        // cung nam tren 1 truc
        // kiem theo hinh chu z
        // kiem theo hinh chu u
        return true;
    }

    public int[][] updateMatrix(int x1, int y1, int x2, int y2, int level) {
        matrix[x1][y1] = -1;
        matrix[x2][y2] = -1;

        switch (level) {
            case 2:
                matrix = gameFlows.shiftDown(matrix);
                break;
            case 3:
                matrix = gameFlows.shiftUp(matrix);
                break;
            case 4:
                matrix = gameFlows.shiftRight(matrix);
                break;
            case 5:
                matrix = gameFlows.shiftLeft(matrix);
                break;
            case 6:
                matrix = gameFlows.shiftInwardX(matrix);
                break;
            case 7:
                matrix = gameFlows.shiftOutwardX(matrix);
                break;
            case 8:
                matrix = gameFlows.shiftInwardY(matrix);
                break;
            case 9:
                matrix = gameFlows.shiftOutwardY(matrix);
                break;
        }
        return matrix;
    }
}
