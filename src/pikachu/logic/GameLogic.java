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
     * Create a valid Pikachu board.
     *
     * Rules:
     * 1. Number of cells must be even
     * 2. Every Pokemon appears in pairs
     * 3. The generated board must contain at least one valid move
     */
    public void createMatrix() {

        // Calculate the number of playable cells
        int totalCells = rows * cols;

        // A Pikachu board must contain an even number of cells
        // because Pokemon are generated in pairs
        if (totalCells % 2 != 0) {
            throw new IllegalArgumentException(
                    "Rows * Cols must be even");
        }

        // Used to determine whether the generated board
        // contains at least one valid move
        boolean validBoard = false;

        // Continue generating boards until a valid one is found
        while (!validBoard) {

            // Re-create matrix to remove old data
            matrix = new int[rows + 2][cols + 2];

            // =====================
            // Create left and right border
            // Border cells contain -1
            // and are considered empty space
            // =====================
            for (int i = 0; i < rows + 2; i++) {
                matrix[i][0] = -1;
                matrix[i][cols + 1] = -1;
            }

            // =====================
            // Create top and bottom border
            // =====================
            for (int j = 0; j < cols + 2; j++) {
                matrix[0][j] = -1;
                matrix[rows + 1][j] = -1;
            }

            // Number of Pokemon pairs required
            int pairCount = totalCells / 2;

            // Store all generated Pokemon IDs
            ArrayList<Integer> values
                    = new ArrayList<>(totalCells);

            // =====================================
            // CASE 1:
            // Enough icons available
            // Example:
            // 30 pairs, 36 icons
            // -> Try to use different icons
            // =====================================
            if (pairCount <= NUM_ICONS) {

                // Store all available icon IDs
                ArrayList<Integer> icons
                        = new ArrayList<>();

                for (int i = 1; i <= NUM_ICONS; i++) {
                    icons.add(i);
                }

                // Randomize icon order
                Collections.shuffle(icons);

                // Select only the required number of icons
                for (int i = 0; i < pairCount; i++) {

                    int icon = icons.get(i);

                    // Add a pair
                    values.add(icon);
                    values.add(icon);
                }
            } // =====================================
            // CASE 2:
            // Not enough icons available
            // Example:
            // 90 pairs but only 36 icons
            // =====================================
            else {

                // Calculate the maximum number of pairs
                // each icon may appear
                int maxPairPerIcon
                        = (int) Math.ceil(
                                (double) pairCount / NUM_ICONS);

                // Temporary container
                ArrayList<Integer> bag
                        = new ArrayList<>();

                // Add each icon multiple times
                for (int icon = 1;
                        icon <= NUM_ICONS;
                        icon++) {

                    for (int k = 0;
                            k < maxPairPerIcon;
                            k++) {

                        bag.add(icon);
                    }
                }

                // Randomize icon distribution
                Collections.shuffle(bag);

                // Select only the number of pairs required
                for (int i = 0;
                        i < pairCount;
                        i++) {

                    int icon = bag.get(i);

                    values.add(icon);
                    values.add(icon);
                }
            }

            // Randomize Pokemon positions
            Collections.shuffle(values);

            // Fill matrix with generated Pokemon IDs
            int index = 0;

            for (int r = 1; r <= rows; r++) {
                for (int c = 1; c <= cols; c++) {

                    matrix[r][c]
                            = values.get(index++);
                }
            }

            // Verify that the board contains
            // at least one valid move
            validBoard = checkValidMatrix();
        }
    }

    /**
     *
     * Check whether the current board still has at least one valid move.
     *
     * Algorithm: 
     * 1. Traverse every cell.
     * 2. Find another cell with the same icon ID 
     * 3. Use BFS (findPath) to determine whether the two cells can be connected 
     * 4. If one valid pair is found, return true 
     * 5. Otherwise return false
     */
    public boolean checkValidMatrix() {

        // Traverse every cell in the board
        for (int i = 1; i <= rows; i++) {

            for (int j = 1; j <= cols; j++) {

                int value = matrix[i][j];

                // Ignore removed cells
                if (value == -1) {
                    continue;
                }

                // Search for another cell
                // containing the same Pokemon
                for (int m = i; m <= rows; m++) {

                    int startCol;

                    // Avoid checking duplicate pairs
                    if (m == i) {
                        startCol = j + 1;
                    } else {
                        startCol = 1;
                    }

                    for (int n = startCol;
                            n <= cols;
                            n++) {

                        // Different Pokemon
                        if (matrix[m][n] != value) {
                            continue;
                        }

                        // Create first Pokemon node
                        PokemonNode p1
                                = new PokemonNode(
                                        i,
                                        j,
                                        value);

                        // Create second Pokemon node
                        PokemonNode p2
                                = new PokemonNode(
                                        m,
                                        n,
                                        value);

                        // Check whether a valid path exists
                        if (findPath(p1, p2)) {

                            // At least one move exists
                            return true;
                        }
                    }
                }
            }
        }

        // No valid pair found
        return false;
    }

    public int[][] getMatrix() {
        return matrix;
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
