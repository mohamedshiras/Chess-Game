package org.example.model;

/**
 * Color - Enumeration for chess piece colors
 */
public enum Color {
    WHITE,
    BLACK;

    /**
     * Get the opposite color
     */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
