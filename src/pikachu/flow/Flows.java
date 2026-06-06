/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pikachu.flow;

/**
 *
 * @author Dung and Giac
 */
public class Flows {

    public Flows() {
    }

    /**
     *
     * @author Dung
     */
    public int[][] shiftDown(int[][] matrix) {
        return matrix;
    }

    public int[][] shiftUp(int[][] matrix) {
        return matrix;
    }

    public int[][] shiftInwardY(int[][] matrix) {

        return matrix;
    }

    public int[][] shiftOutwardY(int[][] matrix) {
        return matrix;
    }

    /**
     *
     * @author Giac
     */
    /**
     * ShiftLeft - Compacts all active elements (ID: 0-35) in each row to the left.
     * Emptry cells(-1) are pushed to the rightmost positions.
     * This boudary avoids touching the outer padding border (Index 0 and size-1).
     * 
     * @param matrix The input 2D array representing the game board.
     * @return The updated matix after applying the leftward flow mechanic.
     */
    public int[][] shiftLeft(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        // Iterate through each row independently
        for (int r = 1; r <= rows - 2; r++) {
            // Track the next available column index to write a active element
            int writePtr = 1;

            // Travese from left to right to find active elements
            for (int c = 1; c <= cols - 2; c++) {
                if (matrix[r][c] != -1) {
                    // Shift the element to the left compact position
                    matrix[r][writePtr] = matrix[r][c];

                    // If the element actually moved, set clear its previous position
                    if (writePtr != c) {
                        matrix[r][c] = -1;
                    }
                    // Move the write pointer to the next position
                    writePtr++;
                }
            }
        }
        // After processing all rows, the matrix will have all non-zero elements shifted
        // left and zeros on the right.
        return matrix;
    }

    /**
     * ShiftRight - Compacts all active elements (ID: 0-35) in each row to the right.
     * Empty cells (-1) are pushed to the leftmost positions.
     * This boudary avoids touching the outer padding border (Index 0 and size-1).
     * 
     * @param matrix The input 2D array representing the game board.
     * @return The updated matrix after applying the rightward flow mechanic.
     */
    public int[][] shiftRight(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        // Iterate through each row independently
        for (int r = 1; r <= rows - 2; r++) {
            // Track the next available column index to write a active element
            int writePtr = cols - 2;

            // Travese from left to right to find active elements
            for (int c = cols - 1; c >= 1; c--) {
                if (matrix[r][c] != -1) {
                    // Shift the element to the right compact position
                    matrix[r][writePtr] = matrix[r][c];

                    // If the element actually moved, set clear its previous position
                    if (writePtr != c) {
                        matrix[r][c] = -1;
                    }
                    // Move the write pointer to the next position
                    writePtr--;
                }
            }
        }
        // After processing all rows, the matrix will have all non-zero elements shifted
        // right and zeros on the left.
        return matrix;
    }

    /**
     * ShiftInwardX - Compacts elements horizontally towards the vertical center
     * line.
     * - Left helf of the matrix shifts RIGHTWARDS towards the center.
     * - Right half of the matrix shifts LEFTWARDS towards the center.
     * Assumes the matrix has an even number of columns for perfact symmetry.
     * 
     * @param matrix The 2D array representing the game board.
     * @return The updated matrix after applying the inward horizontal flow
     *         mechanic.
     */
    public int[][] shiftInwardX(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int mid = cols / 2; // Establish the vertical boundary line

        // Process each row independently as the flow moves horizontally
        for (int r = 1; r <= rows - 2; r++) {

            // SUB-MECHANIC 1: Compact the left helf RIGHTWARDS (towards the center)
            // Start writing from the column right next to center line (mid - 1)
            int writePtrLeft = mid - 1;
            // Traverse backwards from the center line to the left border
            for (int c = mid - 1; c >= 1; c--) {
                if (matrix[r][c] != -1) {
                    matrix[r][writePtrLeft] = matrix[r][c];
                    if (writePtrLeft != c) {
                        matrix[r][c] = -1;
                    }
                    writePtrLeft--; // Move the write pointer leftwards
                }
            }

            // SUB-MECHANIC 2: Compact the right half LEFTWARDS (towards the center)
            // Start writing from the center line (mid)
            int writePtrRight = mid;
            // Traverse forwards from the center line to the right border
            for (int c = mid; c < cols - 1; c++) {
                if (matrix[r][c] != -1) {
                    matrix[r][writePtrRight] = matrix[r][c];
                    if (writePtrRight != c) {
                        matrix[r][c] = -1;
                    }
                    writePtrRight++; // Move the write pointer rightwards
                }
            }
        }
        return matrix;
    }

    /**
     * ShiftOutwardX - Compacts elements horizontally away from the vertical center
     * line.
     * - Left half of the matrix shifts LEFTWARDS towars the left border.
     * - Right half of the matrix shifts RIGHTWARDS towards the right border.
     * Assumes the matrix has an even number of columns for perfact symmetry.
     * 
     * @param matrix The 2D array representing the game board.
     * @return The updated matrix after applying the outward horizontal flow
     *         mechanic.
     */
    public int[][] shiftOutwardX(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int mid = cols / 2; // Establish the vertical boundary line

        // Process each row independently as the flow moves horizontally
        for (int r = 1; r <= rows - 2; r++) {
            // SUB-MECHANIC 1: Compact the left half LEFTWARDS (towards column 0)
            // Start writing from the leftmost border
            int writePtrLeft = 1;
            // Traverse forwards from the left border up to the center line
            for (int c = 1; c < mid; c++) {
                if (matrix[r][c] != -1) {
                    matrix[r][writePtrLeft] = matrix[r][c];
                    if (writePtrLeft != c) {
                        matrix[r][c] = -1;
                    }
                    writePtrLeft++; // Move the write pointer rightwards
                }
            }

            // SUB-MECHANIC 2: Compact the right half RIGHTWARDS (towards the last colummn)
            // Start writing from the rightmost border
            int writePtrRight = cols - 2;
            // Traverse backwards from the right down to the center line
            for (int c = cols - 2; c >= mid; c--) {
                if (matrix[r][c] != -1) {
                    matrix[r][writePtrRight] = matrix[r][c];
                    if (writePtrRight != c) {
                        matrix[r][c] = -1;
                    }
                    writePtrRight--; // Move the write pointer leftwards
                }
            }
        }
        return matrix;
    }
}
