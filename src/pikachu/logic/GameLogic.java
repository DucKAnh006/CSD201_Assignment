/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pikachu.logic;

import pikachu.flow.Flows;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author ADMIN
 */

class Node {
    int row, col;
    int direction; // Hướng đi đến ô này: -1 (bắt đầu), 0 (Lên), 1 (Xuống), 2 (Trái), 3 (Phải)
    int turns;     // Số lần rẽ để đi đến được ô này

    public Node(int row, int col, int direction, int turns) {
        this.row = row;
        this.col = col;
        this.direction = direction;
        this.turns = turns;
    }

    public Node() {
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }


}

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
    /**
     * Trong
     */
    public boolean findPath(PokemonNode pokemon1, PokemonNode pokemon2) {

        // 4 corresponding movement directions: Up, Down, Left, Right
        int[] fx = {0, 0, -1, 1}; // Change on the horizontal axis (Column)
        int[] fy = {-1, 1, 0, 0}; // Change on the vertical axis (Row)

        // 3-dimensional visited marker array: [Column][Row][Direction (0->3)]
        // Helps prevent infinite loops without losing paths that arrive from other directions
        boolean[][][] visited = new boolean[this.cols + 2][this.rows + 2][4];

        Queue<Node> q = new LinkedList<>();

        // Starting point: Direction = -1 (no direction yet), Number of turns = 0
        q.add(new Node(pokemon1.getCol(), pokemon1.getRow(), -1, 0));

        while (!q.isEmpty()) {
            Node currentNode = q.poll();

            // If the second Pokemon's cell has been reached -> Success!
            if (currentNode.col == pokemon2.getCol() && currentNode.row == pokemon2.getRow()) {
                return true;
            }

            // Traverse the 4 directions around the current cell
            for (int i = 0; i < 4; i++) {
                int nextCol = currentNode.col + fx[i];
                int nextRow = currentNode.row + fy[i];

                // 1. Check whether the next cell is within the matrix boundaries
                if (nextCol >= 0 && nextCol < this.cols + 2 && nextRow >= 0 && nextRow < this.rows + 2) {

                    // Calculate the number of turns if changing direction to `i`
                    int nextTurns = currentNode.turns;
                    if (currentNode.direction != -1 && currentNode.direction != i) {
                        nextTurns++;
                    }

                    // Core Pikachu rule: the number of turns must not exceed 2
                    if (nextTurns <= 2) {

                        // Check whether the next cell is an empty cell (-1) or the target cell
                        boolean isEmptySpace = (matrix[nextRow][nextCol] == -1);
                        boolean isTarget = (nextCol == pokemon2.getCol() && nextRow == pokemon2.getRow());

                        if (isEmptySpace || isTarget) {
                            // Check whether this cell has already been visited with this DIRECTION `i`
                            if (!visited[nextCol][nextRow][i]) {
                                visited[nextCol][nextRow][i] = true; // Mark as visited in direction i
                                q.add(new Node(nextCol, nextRow, i, nextTurns));
                            }
                        }
                    }
                }
            }
        }

        // Queue exhausted without finding the target -> No valid path exists
        return false;
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
