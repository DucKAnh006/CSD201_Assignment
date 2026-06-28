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
    
    public GameLogic(int[][] matrix) {
        this.matrix = matrix;
        this.rows = matrix.length - 2;
        this.cols = matrix[0].length - 2;
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
            int n = 0;
            while (values.size() < totalCells) {
                int icon = n % 36;
                values.add(icon);
                if (values.size() < totalCells) {
                    values.add(icon);
                }
                n++;
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
     * Finds the shortest path (with the fewest turns) between two matching Pokemon tiles
     * on the game board using BFS with direction tracking.
     *
     * The algorithm enforces the core Pikachu game rule: a valid connecting path
     * may contain at most 2 turns (i.e., up to 3 straight-line segments). It uses a
     * 3D visited array minTurns[row][col][direction] to prune suboptimal states
     * and backtracks through parent pointers to reconstruct the result.
     *
     * @param pokemon1 the starting Pokemon tile
     * @param pokemon2 the target Pokemon tile (must have the same image ID as pokemon1)
     * @return an ordered list of int[]{row, col} coordinates from source to target
     *         representing the connecting path, or null if no valid path exists
     */
    public ArrayList<int[]> findPath(PokemonNode pokemon1, PokemonNode pokemon2) {

        int[] fx = {0, 0, -1, 1}; // column offsets for 4 directions: up, down, left, right
        int[] fy = {-1, 1, 0, 0}; // row offsets for 4 directions: up, down, left, right

        int[][][] minTurns = new int[this.rows + 2][this.cols + 2][4]; // tracks min turns to reach each cell from each direction
        for (int[][] layer : minTurns)
            for (int[] row : layer)
                Arrays.fill(row, Integer.MAX_VALUE); // initialize all cells as unvisited

        Queue<Node> q = new LinkedList<>();

        q.add(new Node(pokemon1.getRow(), pokemon1.getCol(), -1, 0, null)); // enqueue start cell with no initial direction

        Node bestEnd = null; // stores the best path found to the target

        while (!q.isEmpty()) {
            Node currentNode = q.poll(); // dequeue the next node to process

            if (currentNode.col == pokemon2.getCol() && currentNode.row == pokemon2.getRow()) { // reached the target cell
                if (bestEnd == null || currentNode.turns < bestEnd.turns) {
                    bestEnd = currentNode; // update best result if this path has fewer turns
                }
                continue; // continue BFS to find potentially better paths
            }

            for (int i = 0; i < 4; i++) { // explore all 4 adjacent directions
                int nextCol = currentNode.col + fx[i];
                int nextRow = currentNode.row + fy[i];

                if (nextCol >= 0 && nextCol < this.cols + 2 && nextRow >= 0 && nextRow < this.rows + 2) { // bounds check

                    int nextTurns = currentNode.turns; // carry forward current turn count
                    if (currentNode.direction != -1 && currentNode.direction != i) {
                        nextTurns++; // increment turns when direction changes
                    }

                    if (nextTurns <= 2) { // Pikachu rule: path can have at most 2 turns

                        boolean isEmptySpace = (matrix[nextRow][nextCol] == -1); // cell is walkable
                        boolean isTarget = (nextCol == pokemon2.getCol() && nextRow == pokemon2.getRow()); // cell is the destination

                        if (isEmptySpace || isTarget) {
                            if (nextTurns < minTurns[nextRow][nextCol][i]) { // only proceed if this is a better route
                                minTurns[nextRow][nextCol][i] = nextTurns; // record the improved turn count
                                q.add(new Node(nextRow, nextCol, i, nextTurns, currentNode)); // enqueue neighbor for further exploration
                            }
                        }
                    }
                }
            }
        }

        if (bestEnd == null) {
            return null; // no valid path found
        }

        ArrayList<int[]> path = new ArrayList<>(); // reconstruct path by backtracking through parent pointers
        Node trace = bestEnd;
        
        while (trace != null) {
            path.add(new int[]{trace.row, trace.col}); // add each node's position to the path
            trace = trace.parent;
        }
        Collections.reverse(path); // reverse to get source-to-target order
        
        return path;
    }




    /**
     * Updates the game matrix after a successful tile match by removing the two matched
     * tiles and applying a level-specific gravity or flow effect.
     *
     * The matched cells are set to -1 (empty), then the remaining tiles are
     * shifted according to the current game level:
     *   - Level 2: tiles shift down
     *   - Level 3: tiles shift up
     *   - Level 4: tiles shift right
     *   - Level 5: tiles shift left
     *   - Level 6: tiles collapse inward horizontally
     *   - Level 7: tiles expand outward horizontally
     *   - Level 8: tiles collapse inward vertically
     *   - Level 9: tiles expand outward vertically
     *
     * @param x1    row index of the first matched tile
     * @param y1    column index of the first matched tile
     * @param x2    row index of the second matched tile
     * @param y2    column index of the second matched tile
     * @param level the current game level that determines the flow effect
     * @return the updated game matrix after removal and shifting
     */
    public int[][] updateMatrix(int x1, int y1, int x2, int y2, int level) {
        matrix[x1][y1] = -1; // mark first matched cell as empty
        matrix[x2][y2] = -1; // mark second matched cell as empty

        switch (level) { // apply gravity/flow effect based on the current game level
            case 2:
                matrix = gameFlows.shiftDown(matrix); // tiles fall downward
                break;
            case 3:
                matrix = gameFlows.shiftUp(matrix); // tiles float upward
                break;
            case 4:
                matrix = gameFlows.shiftRight(matrix); // tiles slide to the right
                break;
            case 5:
                matrix = gameFlows.shiftLeft(matrix); // tiles slide to the left
                break;
            case 6:
                matrix = gameFlows.shiftInwardX(matrix); // tiles collapse horizontally toward center
                break;
            case 7:
                matrix = gameFlows.shiftOutwardX(matrix); // tiles expand horizontally from center
                break;
            case 8:
                matrix = gameFlows.shiftInwardY(matrix); // tiles collapse vertically toward center
                break;
            case 9:
                matrix = gameFlows.shiftOutwardY(matrix); // tiles expand vertically from center
                break;
        }
        return matrix; // return the updated board state
    }
}
