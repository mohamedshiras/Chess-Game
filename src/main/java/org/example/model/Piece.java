package org.example.model;

/**
 * Piece - Represents a chess piece
 * 
 * Contains:
 * - Type (King, Queen, Rook, Bishop, Knight, Pawn)
 * - Color (White or Black)
 * - Current position on the board
 * - Whether piece has moved (for castling and pawn double-move)
 * 
 * Pieces are stored in Singly Linked Lists for:
 * - Active pieces (pieces still on the board)
 * - Captured pieces (pieces that have been taken)
 */
public class Piece {

    private PieceType type;
    private Color color;
    private Position position;
    private boolean hasMoved;

    // Unique ID for each piece (helps with identification in linked lists)
    private final String id;
    private static int idCounter = 0;

    public Piece(PieceType type, Color color, Position position) {
        this.type = type;
        this.color = color;
        this.position = position;
        this.hasMoved = false;
        this.id = color.toString().charAt(0) + type.getSymbol() + (++idCounter);
    }

    public PieceType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public String getId() {
        return id;
    }

    /**
     * Reset ID counter (for new games)
     */
    public static void resetIdCounter() {
        idCounter = 0;
    }

    /**
     * Get Unicode symbol for display
     */
    public String getUnicodeSymbol() {
        if (color == Color.WHITE) {
            switch (type) {
                case KING:
                    return "♔";
                case QUEEN:
                    return "♕";
                case ROOK:
                    return "♖";
                case BISHOP:
                    return "♗";
                case KNIGHT:
                    return "♘";
                case PAWN:
                    return "♙";
            }
        } else {
            switch (type) {
                case KING:
                    return "♚";
                case QUEEN:
                    return "♛";
                case ROOK:
                    return "♜";
                case BISHOP:
                    return "♝";
                case KNIGHT:
                    return "♞";
                case PAWN:
                    return "♟";
            }
        }
        return "?";
    }

    /**
     * Get simple symbol for JSON/API
     */
    public String getSymbol() {
        String symbol = type.getSymbol();
        return color == Color.WHITE ? symbol : symbol.toLowerCase();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Piece piece = (Piece) obj;
        return id.equals(piece.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return color.toString().charAt(0) + "-" + type.getSymbol() + "@" + position.toNotation();
    }
}
