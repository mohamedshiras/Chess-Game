package org.example.model;

/**
 * PieceType - Enumeration of all chess piece types
 * 
 * Each piece type has:
 * - A symbol for display (Unicode chess symbols)
 * - A value for potential scoring/evaluation
 */
public enum PieceType {
    KING("K", 0),
    QUEEN("Q", 9),
    ROOK("R", 5),
    BISHOP("B", 3),
    KNIGHT("N", 3),
    PAWN("P", 1);

    private final String symbol;
    private final int value;

    PieceType(String symbol, int value) {
        this.symbol = symbol;
        this.value = value;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getValue() {
        return value;
    }
}
