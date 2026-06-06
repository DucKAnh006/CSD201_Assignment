/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pikachu.flow;

/**
 *
 * @author ADMIN
 */
public class Flows {

    public Flows() {
    }

    /**
     * Dung
     */
    public int[][] shiftDown(int[][] matrix) {
        return matrix;
    }

    public int[][] shiftUp(int[][] matrix) {
        return matrix;
    }

    public int[][] shiftInwardX(int[][] matrix) {
        return matrix;
    }

    public int[][] shiftOutwardX(int[][] matrix) {
        return matrix;
    }

    /**
     * ShiftLeft - Compacts all non-zero elements in each row to the left.
     * Zeros (emptry cells) are pushed to the rightmost positions.
     * 
     * @param matrix The input 2D array representing the game board.
     * @return The updated matix after applying the leftward flow mechanic.
     */
    public int[][] shiftLeft(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        // Iterate through each row independently
        for (int r = 0; r < rows; r++) {
            // Track the next available column index to write a non-zero element
            int writePtr = 0;

            // Travese from left to right to find active elements
            for (int c = 0; c < cols; c++) {
                if (matrix[r][c] != 0) {
                    // Shift the element to the left compact position
                    matrix[r][writePtr] = matrix[r][c];

                    // If the element actually moved, set clear its previous position
                    if (writePtr != c) {
                        matrix[r][c] = 0;
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

    public int[][] shiftRight(int[][] matrix) {
        return matrix;
    }

    public int[][] shiftInwardY(int[][] matrix) {
        return matrix;
    }

    public int[][] shiftOutwardY(int[][] matrix) {
        return matrix;
    }

}
