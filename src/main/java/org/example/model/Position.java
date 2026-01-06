package org.example.model;

/**
 * Position - Represents a position on the chess board
 * 
 * Chess board positions use:
 * - Column (file): 0-7 representing a-h
 * - Row (rank): 0-7 representing 1-8
 * 
 * Example: Position(0, 0) = a1, Position(7, 7) = h8
 */
public class Position {

    private int row; // 0-7 (rank 1-8)
    private int col; // 0-7 (file a-h)

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Create position from chess notation (e.g., "e4")
     */
    public static Position fromNotation(String notation) {
        if (notation == null || notation.length() != 2) {
            return null;
        }
        char file = notation.charAt(0);
        char rank = notation.charAt(1);

        int col = file - 'a';
        int row = rank - '1';

        if (row < 0 || row > 7 || col < 0 || col > 7) {
            return null;
        }

        return new Position(row, col);
    }

    /**
     * Convert to chess notation (e.g., "e4")
     */
    public String toNotation() {
        char file = (char) ('a' + col);
        char rank = (char) ('1' + row);
        return "" + file + rank;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Check if position is within board bounds
     */
    public boolean isValid() {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Position position = (Position) obj;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return row * 8 + col;
    }

    @Override
    public String toString() {
        return toNotation();
    }
}
