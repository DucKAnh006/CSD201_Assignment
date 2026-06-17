/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pikachu.logic;

import pikachu.flow.Flows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author ADMIN
 */

/**
 * Represents a cell used during pathfinding.
 * Stores the cell position, the direction used to reach it,
 * and the number of turns made so far.
 */
class Node {
    int row, col;
    int direction; // Direction used to reach this node: -1 = start, 0 = up, 1 = down, 2 = left, 3 = right
    int turns;     // Number of turns made to reach this node
    Node parent;   // Parent node for path backtracking

    public Node(int row, int col, int direction, int turns, Node parent) {
        this.row = row;
        this.col = col;
        this.direction = direction;
        this.turns = turns;
        this.parent = parent;
    }

    public Node() {
    }

    public int getDirection() {
        return direction;
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
     * Generates a new Pikachu game board.
     *
     * This method creates pairs of Pokemon IDs, randomly shuffles them, places
     * them into the playable area, and surrounds the board with a border of
     * empty cells (-1).
     *
     * The generation process repeats until the board contains at least one
     * valid matching pair that can be connected according to the Pikachu
     * findPath rules.
     *
     * @throws IllegalArgumentException if rows * cols is odd
     */
    public void createMatrix() {

        // Total number of playable cells
        int totalCells = rows * cols;

        // The number of cells must be even so that all Pokemon can be generated in pairs
        if (totalCells % 2 != 0) {
            throw new IllegalArgumentException("Rows * Cols must be even");
        }
        
        boolean validBoard = false;
        
        // Continue generating boards until at least one valid move exists
        while (!validBoard) {

            // Store all Pokemon IDs that will be placed on the board
            ArrayList<Integer> values = new ArrayList<>(totalCells);

            // Generate Pokemon IDs in pairs
            // Example: 3 3 8 8 12 12 ...
            while (values.size() < totalCells) {
                int icon = (int) (Math.random() * 36);
                values.add(icon);
                if (values.size() < totalCells) {
                    values.add(icon);
                }
            }

            // Randomize the order of all generated Pokemon pairs
            Collections.shuffle(values);
            int index = 0;

            // Fill the matrix:
            // - Border cells are marked as -1
            // - Inner cells contain Pokemon IDs
            for (int r = 0; r < rows + 2; r++) {
                for (int c = 0; c < cols + 2; c++) {
                    if (r == 0 || r == rows + 1 || c == 0 || c == cols + 1) {
                        matrix[r][c] = -1;
                    } else {
                        matrix[r][c] = values.get(index++);
                    }
                }
            }

            // Verify that the generated board contains at least one valid move
            validBoard = checkValidMatrix();
        }
    }

    /**
     * Checks whether the current board contains at least one valid move.
     *
     * The method scans all remaining Pokemon tiles and searches for another
     * tile with the same ID. For each matching pair, the findPath algorithm is
     * executed to determine whether the two tiles can be connected with at most
     * two turns.
     *
     * If at least one connectable pair exists, the board is considered valid
     * and the method returns {@code true}.
     *
     * @return {@code true} if at least one valid pair exists; {@code false}
     * otherwise
     */
    public boolean checkValidMatrix() {

        // Traverse every active cell on the board
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {

                // Ignore empty cells
                if (matrix[i][j] == -1) {
                    continue;
                }
                int value = matrix[i][j];

                // Search for another Pokemon with the same ID
                for (int m = i; m <= rows; m++) {
                    int startCol = (m == i) ? j + 1 : 1;

                    // Avoid checking the same pair twice
                    for (int n = startCol; n <= cols; n++) {
                        if (matrix[m][n] != value) {
                            continue;
                        }

                        // Create two Pokemon nodes for pathfinding
                        PokemonNode p1 = new PokemonNode(i, j, value);
                        PokemonNode p2 = new PokemonNode(m, n, value);

                        // If a valid path exists between the two Pokemon,
                        // the board still has at least one available move
                        if (findPath(p1, p2) != null) {
                            return true;
                        }
                    }
                }
            }
        }

        // No connectable pair was found
        return false;
    }
    
    /**
     * Shuffles all remaining active Pokemon tiles on the board.
     * This method collects the IDs of all remaining tiles, shuffles them randomly, 
     * and redistributes them into the active slots to resolve deadlocks. It repeats 
     * the shuffle if the resulting board state has no valid moves left.
     * * @return int[][] The updated game matrix after shuffling.
     */
    public int[][] swapMatrix() {
    
        // Store all remaining Pokemon IDs that are still available on the board
        ArrayList<Integer> validPokemonIds = new ArrayList<>();

        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                
                if (matrix[i][j] != -1) {
                    validPokemonIds.add(matrix[i][j]);
                }
            }
        }

       
        // Randomly shuffle the remaining Pokemon IDs
        Collections.shuffle(validPokemonIds);

        // Put the shuffled Pokemon IDs back into the available cells
        int index = 0;
        for (int i = 1; i <= rows; i++) {
            for (int j = 1; j <= cols; j++) {
                // Fill only cells that are still active
                if (matrix[i][j] != -1) {
                    matrix[i][j] = validPokemonIds.get(index);
                    index++;
                }
            }
        }

        // If the shuffled board has no valid moves, shuffle again
        if (!checkValidMatrix()){
            swapMatrix();
        }

        // Return the updated board
        return matrix;
    }

    

    /**
     * Finds the path with the fewest turns between two matching Pokemon tiles.
     * Uses BFS with direction tracking and parent backtracking.
     *
     * @param pokemon1 the starting Pokemon tile
     * @param pokemon2 the target Pokemon tile
     * @return ArrayList<int[]> if a valid path exists; null otherwise
     */
    public ArrayList<int[]> findPath(PokemonNode pokemon1, PokemonNode pokemon2) {

        // 4 corresponding movement directions: Up, Down, Left, Right
        int[] fx = {0, 0, -1, 1}; // Change on the horizontal axis (Column)
        int[] fy = {-1, 1, 0, 0}; // Change on the vertical axis (Row)

        // minTurns[row][col][dir] = minimum turns to reach (row,col) from direction dir
        int[][][] minTurns = new int[this.rows + 2][this.cols + 2][4];
        for (int[][] layer : minTurns)
            for (int[] row : layer)
                Arrays.fill(row, Integer.MAX_VALUE);

        Queue<Node> q = new LinkedList<>();

        // Starting point: Direction = -1 (no direction yet), Number of turns = 0, no parent
        q.add(new Node(pokemon1.getRow(), pokemon1.getCol(), -1, 0, null));

        Node bestEnd = null;

        while (!q.isEmpty()) {
            Node currentNode = q.poll();

            // If the second Pokemon's cell has been reached
            if (currentNode.col == pokemon2.getCol() && currentNode.row == pokemon2.getRow()) {
                if (bestEnd == null || currentNode.turns < bestEnd.turns) {
                    bestEnd = currentNode;
                }
                continue; // Keep searching for a path with fewer turns
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
                            // Only enqueue if we found a better (fewer turns) way to reach this cell
                            if (nextTurns < minTurns[nextRow][nextCol][i]) {
                                minTurns[nextRow][nextCol][i] = nextTurns;
                                q.add(new Node(nextRow, nextCol, i, nextTurns, currentNode));
                            }
                        }
                    }
                }
            }
        }

        // Backtrack from target to source using parent pointers
        if (bestEnd == null) {
            return null; // Không tìm thấy đường đi nào hợp lệ
        }

        ArrayList<int[]> path = new ArrayList<>(); 
        Node trace = bestEnd;
        
        while (trace != null) {
            path.add(new int[]{trace.row, trace.col});
            trace = trace.parent;
        }
        Collections.reverse(path); // Reverse to get source -> target order
        
        // Trả trực tiếp kết quả, không lưu vào lastPath nữa
        return path;
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
