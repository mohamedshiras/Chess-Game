package org.example.model;

/**
 * Move - Represents a chess move
 * 
 * This class is stored in the DOUBLY LINKED LIST for move history.
 * Each move node allows traversal:
 * - Backwards (previous) for UNDO
 * - Forwards (next) for REDO
 * 
 * Contains all information needed to:
 * - Apply the move
 * - Undo the move (restore captured piece, restore positions)
 */
public class Move {

    // Starting position of the move
    private Position from;

    // Ending position of the move
    private Position to;

    // The piece that was moved
    private Piece piece;

    // The piece that was captured (null if no capture)
    private Piece capturedPiece;

    // Whether this was the piece's first move (for undo purposes)
    private boolean wasFirstMove;

    // Special move types
    private boolean isCastling;
    private boolean isEnPassant;
    private boolean isPromotion;
    private PieceType promotionType;

    // For castling - rook details
    private Position rookFrom;
    private Position rookTo;

    /**
     * Constructor for a regular move
     */
    public Move(Position from, Position to, Piece piece, Piece capturedPiece) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.wasFirstMove = !piece.hasMoved();
        this.isCastling = false;
        this.isEnPassant = false;
        this.isPromotion = false;
    }

    // Getters

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getPiece() {
        return piece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean wasFirstMove() {
        return wasFirstMove;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public void setCastling(boolean castling, Position rookFrom, Position rookTo) {
        this.isCastling = castling;
        this.rookFrom = rookFrom;
        this.rookTo = rookTo;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public void setEnPassant(boolean enPassant) {
        this.isEnPassant = enPassant;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    public void setPromotion(boolean promotion, PieceType promotionType) {
        this.isPromotion = promotion;
        this.promotionType = promotionType;
    }

    public PieceType getPromotionType() {
        return promotionType;
    }

    public Position getRookFrom() {
        return rookFrom;
    }

    public Position getRookTo() {
        return rookTo;
    }

    /**
     * Get move in algebraic notation (simplified)
     */
    public String getNotation() {
        StringBuilder sb = new StringBuilder();

        if (isCastling) {
            if (to.getCol() > from.getCol()) {
                return "O-O"; // Kingside
            } else {
                return "O-O-O"; // Queenside
            }
        }

        // Piece symbol (except for pawns)
        if (piece.getType() != PieceType.PAWN) {
            sb.append(piece.getType().getSymbol());
        }

        // From square
        sb.append(from.toNotation());

        // Capture indicator
        if (capturedPiece != null) {
            sb.append("x");
        } else {
            sb.append("-");
        }

        // To square
        sb.append(to.toNotation());

        // Promotion
        if (isPromotion) {
            sb.append("=").append(promotionType.getSymbol());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getNotation();
    }
}
