package org.example.game;

import org.example.datastructures.SinglyLinkedList;
import org.example.datastructures.SinglyNode;
import org.example.model.*;

/**
 * ChessBoard - Manages the chess board state and move validation
 * 
 * LINKED LIST USAGE:
 * - Uses SinglyLinkedList for active pieces (white and black separately)
 * - Board array is ONLY used for position lookup (O(1) access)
 * - All piece management is done through linked lists
 * 
 * The board uses an 8x8 grid where:
 * - Row 0 = Rank 1 (white's back rank)
 * - Row 7 = Rank 8 (black's back rank)
 * - Col 0 = File a
 * - Col 7 = File h
 */
public class ChessBoard {

    // Board grid - ONLY for O(1) position lookup, NOT for storage
    // Pieces are stored in linked lists below
    private Piece[][] board;

    /**
     * SINGLY LINKED LIST: Active white pieces
     * Used to efficiently iterate over white's pieces without scanning entire board
     * When a piece is captured, it's removed from this list
     */
    private SinglyLinkedList<Piece> whitePieces;

    /**
     * SINGLY LINKED LIST: Active black pieces
     * Used to efficiently iterate over black's pieces without scanning entire board
     * When a piece is captured, it's removed from this list
     */
    private SinglyLinkedList<Piece> blackPieces;

    // Track kings for check detection
    private Piece whiteKing;
    private Piece blackKing;

    // En passant target square (set when a pawn moves two squares)
    private Position enPassantTarget;

    public ChessBoard() {
        board = new Piece[8][8];
        whitePieces = new SinglyLinkedList<>();
        blackPieces = new SinglyLinkedList<>();
        initializeBoard();
    }

    /**
     * Set up the initial chess position
     * All pieces are added to the appropriate linked list
     */
    private void initializeBoard() {
        // Clear the board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }

        // Clear linked lists
        whitePieces.clear();
        blackPieces.clear();

        // Reset piece ID counter
        Piece.resetIdCounter();

        // Place white pieces (Row 0 = Rank 1)
        placePiece(new Piece(PieceType.ROOK, Color.WHITE, new Position(0, 0)));
        placePiece(new Piece(PieceType.KNIGHT, Color.WHITE, new Position(0, 1)));
        placePiece(new Piece(PieceType.BISHOP, Color.WHITE, new Position(0, 2)));
        placePiece(new Piece(PieceType.QUEEN, Color.WHITE, new Position(0, 3)));
        whiteKing = new Piece(PieceType.KING, Color.WHITE, new Position(0, 4));
        placePiece(whiteKing);
        placePiece(new Piece(PieceType.BISHOP, Color.WHITE, new Position(0, 5)));
        placePiece(new Piece(PieceType.KNIGHT, Color.WHITE, new Position(0, 6)));
        placePiece(new Piece(PieceType.ROOK, Color.WHITE, new Position(0, 7)));

        // White pawns (Row 1 = Rank 2)
        for (int col = 0; col < 8; col++) {
            placePiece(new Piece(PieceType.PAWN, Color.WHITE, new Position(1, col)));
        }

        // Place black pieces (Row 7 = Rank 8)
        placePiece(new Piece(PieceType.ROOK, Color.BLACK, new Position(7, 0)));
        placePiece(new Piece(PieceType.KNIGHT, Color.BLACK, new Position(7, 1)));
        placePiece(new Piece(PieceType.BISHOP, Color.BLACK, new Position(7, 2)));
        placePiece(new Piece(PieceType.QUEEN, Color.BLACK, new Position(7, 3)));
        blackKing = new Piece(PieceType.KING, Color.BLACK, new Position(7, 4));
        placePiece(blackKing);
        placePiece(new Piece(PieceType.BISHOP, Color.BLACK, new Position(7, 5)));
        placePiece(new Piece(PieceType.KNIGHT, Color.BLACK, new Position(7, 6)));
        placePiece(new Piece(PieceType.ROOK, Color.BLACK, new Position(7, 7)));

        // Black pawns (Row 6 = Rank 7)
        for (int col = 0; col < 8; col++) {
            placePiece(new Piece(PieceType.PAWN, Color.BLACK, new Position(6, col)));
        }

        enPassantTarget = null;
    }

    /**
     * Place a piece on the board
     * Adds to board grid AND to the appropriate linked list
     */
    private void placePiece(Piece piece) {
        Position pos = piece.getPosition();
        board[pos.getRow()][pos.getCol()] = piece;

        // Add to appropriate linked list
        if (piece.getColor() == Color.WHITE) {
            whitePieces.addLast(piece);
        } else {
            blackPieces.addLast(piece);
        }
    }

    /**
     * Get piece at position
     */
    public Piece getPieceAt(Position pos) {
        if (!pos.isValid()) {
            return null;
        }
        return board[pos.getRow()][pos.getCol()];
    }

    /**
     * Get piece at position using row and column
     */
    public Piece getPieceAt(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            return null;
        }
        return board[row][col];
    }

    /**
     * Check if a move is valid
     */
    public boolean isValidMove(Position from, Position to, Color playerColor) {
        Piece piece = getPieceAt(from);

        // Basic validation
        if (piece == null) {
            return false;
        }
        if (piece.getColor() != playerColor) {
            return false;
        }
        if (from.equals(to)) {
            return false;
        }
        if (!to.isValid()) {
            return false;
        }

        // Can't capture own piece
        Piece targetPiece = getPieceAt(to);
        if (targetPiece != null && targetPiece.getColor() == playerColor) {
            return false;
        }

        // Check if the move is valid for this piece type
        if (!isValidPieceMove(piece, from, to)) {
            return false;
        }

        // Check if move would leave king in check
        if (wouldBeInCheck(piece, from, to, playerColor)) {
            return false;
        }

        return true;
    }

    /**
     * Validate move based on piece type movement rules
     */
    private boolean isValidPieceMove(Piece piece, Position from, Position to) {
        int rowDiff = to.getRow() - from.getRow();
        int colDiff = to.getCol() - from.getCol();
        int absRowDiff = Math.abs(rowDiff);
        int absColDiff = Math.abs(colDiff);

        switch (piece.getType()) {
            case PAWN:
                return isValidPawnMove(piece, from, to, rowDiff, colDiff);
            case ROOK:
                return isValidRookMove(from, to);
            case KNIGHT:
                return (absRowDiff == 2 && absColDiff == 1) || (absRowDiff == 1 && absColDiff == 2);
            case BISHOP:
                return isValidBishopMove(from, to);
            case QUEEN:
                return isValidRookMove(from, to) || isValidBishopMove(from, to);
            case KING:
                return isValidKingMove(piece, from, to, absRowDiff, absColDiff);
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(Piece pawn, Position from, Position to, int rowDiff, int colDiff) {
        int direction = pawn.getColor() == Color.WHITE ? 1 : -1;
        Piece targetPiece = getPieceAt(to);

        // Forward move
        if (colDiff == 0) {
            if (targetPiece != null) {
                return false; // Can't capture forward
            }
            // Single step forward
            if (rowDiff == direction) {
                return true;
            }
            // Double step from starting position
            if (rowDiff == 2 * direction) {
                int startRow = pawn.getColor() == Color.WHITE ? 1 : 6;
                if (from.getRow() == startRow) {
                    // Check path is clear
                    Position middle = new Position(from.getRow() + direction, from.getCol());
                    return getPieceAt(middle) == null;
                }
            }
            return false;
        }

        // Diagonal capture
        if (Math.abs(colDiff) == 1 && rowDiff == direction) {
            // Normal capture
            if (targetPiece != null && targetPiece.getColor() != pawn.getColor()) {
                return true;
            }
            // En passant
            if (enPassantTarget != null && to.equals(enPassantTarget)) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidRookMove(Position from, Position to) {
        // Must move in straight line
        if (from.getRow() != to.getRow() && from.getCol() != to.getCol()) {
            return false;
        }
        // Check path is clear
        return isPathClear(from, to);
    }

    private boolean isValidBishopMove(Position from, Position to) {
        int absRowDiff = Math.abs(to.getRow() - from.getRow());
        int absColDiff = Math.abs(to.getCol() - from.getCol());

        // Must move diagonally
        if (absRowDiff != absColDiff) {
            return false;
        }
        // Check path is clear
        return isPathClear(from, to);
    }

    private boolean isValidKingMove(Piece king, Position from, Position to, int absRowDiff, int absColDiff) {
        // Normal king move (one square)
        if (absRowDiff <= 1 && absColDiff <= 1) {
            return true;
        }

        // Castling
        if (absRowDiff == 0 && absColDiff == 2 && !king.hasMoved()) {
            return canCastle(king, to);
        }

        return false;
    }

    /**
     * Check if castling is possible
     */
    private boolean canCastle(Piece king, Position to) {
        Color color = king.getColor();
        int row = king.getPosition().getRow();

        // Check if king is in check
        if (isInCheck(color)) {
            return false;
        }

        // Determine kingside or queenside
        boolean kingside = to.getCol() > king.getPosition().getCol();
        int rookCol = kingside ? 7 : 0;

        // Check rook exists and hasn't moved
        Piece rook = getPieceAt(row, rookCol);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.hasMoved()) {
            return false;
        }

        // Check path is clear
        int startCol = Math.min(king.getPosition().getCol(), rookCol) + 1;
        int endCol = Math.max(king.getPosition().getCol(), rookCol);
        for (int col = startCol; col < endCol; col++) {
            if (getPieceAt(row, col) != null) {
                return false;
            }
        }

        // Check king doesn't pass through check
        int direction = kingside ? 1 : -1;
        Position midPos = new Position(row, king.getPosition().getCol() + direction);
        if (isSquareAttacked(midPos, color.opposite())) {
            return false;
        }
        if (isSquareAttacked(to, color.opposite())) {
            return false;
        }

        return true;
    }

    /**
     * Check if path between two positions is clear (for rook, bishop, queen)
     */
    private boolean isPathClear(Position from, Position to) {
        int rowDirection = Integer.compare(to.getRow(), from.getRow());
        int colDirection = Integer.compare(to.getCol(), from.getCol());

        int currentRow = from.getRow() + rowDirection;
        int currentCol = from.getCol() + colDirection;

        while (currentRow != to.getRow() || currentCol != to.getCol()) {
            if (getPieceAt(currentRow, currentCol) != null) {
                return false;
            }
            currentRow += rowDirection;
            currentCol += colDirection;
        }

        return true;
    }

    /**
     * Check if a square is attacked by the given color
     */
    public boolean isSquareAttacked(Position pos, Color attackingColor) {
        SinglyLinkedList<Piece> pieces = attackingColor == Color.WHITE ? whitePieces : blackPieces;
        SinglyNode<Piece> current = pieces.getHead();

        while (current != null) {
            Piece piece = current.getData();
            if (isValidPieceMove(piece, piece.getPosition(), pos)) {
                return true;
            }
            current = current.getNext();
        }

        return false;
    }

    /**
     * Check if a color's king is in check
     */
    public boolean isInCheck(Color color) {
        Piece king = color == Color.WHITE ? whiteKing : blackKing;
        return isSquareAttacked(king.getPosition(), color.opposite());
    }

    /**
     * Check if a move would leave the player's king in check
     */
    private boolean wouldBeInCheck(Piece piece, Position from, Position to, Color playerColor) {
        // Make temporary move
        Piece capturedPiece = getPieceAt(to);

        board[from.getRow()][from.getCol()] = null;
        board[to.getRow()][to.getCol()] = piece;
        Position originalPos = piece.getPosition();
        piece.setPosition(to);

        // Remove captured piece from list temporarily
        if (capturedPiece != null) {
            if (capturedPiece.getColor() == Color.WHITE) {
                whitePieces.remove(capturedPiece);
            } else {
                blackPieces.remove(capturedPiece);
            }
        }

        // Check if king is in check
        boolean inCheck = isInCheck(playerColor);

        // Undo temporary move
        board[from.getRow()][from.getCol()] = piece;
        board[to.getRow()][to.getCol()] = capturedPiece;
        piece.setPosition(originalPos);

        // Restore captured piece to list
        if (capturedPiece != null) {
            if (capturedPiece.getColor() == Color.WHITE) {
                whitePieces.addLast(capturedPiece);
            } else {
                blackPieces.addLast(capturedPiece);
            }
        }

        return inCheck;
    }

    /**
     * Execute a move on the board
     * Returns the Move object with all details for history
     */
    public Move executeMove(Position from, Position to) {
        Piece piece = getPieceAt(from);
        Piece capturedPiece = getPieceAt(to);

        Move move = new Move(from, to, piece, capturedPiece);

        // Handle en passant capture
        if (piece.getType() == PieceType.PAWN && to.equals(enPassantTarget)) {
            int capturedPawnRow = piece.getColor() == Color.WHITE ? to.getRow() - 1 : to.getRow() + 1;
            capturedPiece = getPieceAt(capturedPawnRow, to.getCol());
            board[capturedPawnRow][to.getCol()] = null;
            move = new Move(from, to, piece, capturedPiece);
            move.setEnPassant(true);
        }

        // Handle castling
        if (piece.getType() == PieceType.KING && Math.abs(to.getCol() - from.getCol()) == 2) {
            boolean kingside = to.getCol() > from.getCol();
            int rookFromCol = kingside ? 7 : 0;
            int rookToCol = kingside ? 5 : 3;

            Piece rook = getPieceAt(from.getRow(), rookFromCol);
            Position rookFrom = new Position(from.getRow(), rookFromCol);
            Position rookTo = new Position(from.getRow(), rookToCol);

            // Move rook
            board[from.getRow()][rookFromCol] = null;
            board[from.getRow()][rookToCol] = rook;
            rook.setPosition(rookTo);
            rook.setHasMoved(true);

            move.setCastling(true, rookFrom, rookTo);
        }

        // Remove captured piece from active pieces list
        if (capturedPiece != null) {
            if (capturedPiece.getColor() == Color.WHITE) {
                whitePieces.remove(capturedPiece);
            } else {
                blackPieces.remove(capturedPiece);
            }
        }

        // Move the piece
        board[from.getRow()][from.getCol()] = null;
        board[to.getRow()][to.getCol()] = piece;
        piece.setPosition(to);
        piece.setHasMoved(true);

        // Handle pawn promotion (auto-promote to Queen for simplicity)
        if (piece.getType() == PieceType.PAWN) {
            int promotionRow = piece.getColor() == Color.WHITE ? 7 : 0;
            if (to.getRow() == promotionRow) {
                // Create new queen
                Piece queen = new Piece(PieceType.QUEEN, piece.getColor(), to);
                queen.setHasMoved(true);

                // Replace pawn with queen on board
                board[to.getRow()][to.getCol()] = queen;

                // Update linked list
                if (piece.getColor() == Color.WHITE) {
                    whitePieces.remove(piece);
                    whitePieces.addLast(queen);
                } else {
                    blackPieces.remove(piece);
                    blackPieces.addLast(queen);
                }

                move.setPromotion(true, PieceType.QUEEN);
            }
        }

        // Update en passant target
        if (piece.getType() == PieceType.PAWN && Math.abs(to.getRow() - from.getRow()) == 2) {
            int epRow = piece.getColor() == Color.WHITE ? from.getRow() + 1 : from.getRow() - 1;
            enPassantTarget = new Position(epRow, from.getCol());
        } else {
            enPassantTarget = null;
        }

        return move;
    }

    /**
     * Undo a move on the board
     */
    public void undoMove(Move move) {
        Piece piece = move.getPiece();
        Position from = move.getFrom();
        Position to = move.getTo();

        // Handle promotion - need to restore pawn
        if (move.isPromotion()) {
            Piece promotedPiece = getPieceAt(to);
            if (promotedPiece != null) {
                if (piece.getColor() == Color.WHITE) {
                    whitePieces.remove(promotedPiece);
                    whitePieces.addLast(piece);
                } else {
                    blackPieces.remove(promotedPiece);
                    blackPieces.addLast(piece);
                }
            }
        }

        // Move piece back
        board[to.getRow()][to.getCol()] = null;
        board[from.getRow()][from.getCol()] = piece;
        piece.setPosition(from);

        // Restore hasMoved status
        if (move.wasFirstMove()) {
            piece.setHasMoved(false);
        }

        // Restore captured piece
        Piece capturedPiece = move.getCapturedPiece();
        if (capturedPiece != null) {
            Position capturePos = move.isEnPassant()
                    ? new Position(to.getRow() + (piece.getColor() == Color.WHITE ? -1 : 1), to.getCol())
                    : to;

            board[capturePos.getRow()][capturePos.getCol()] = capturedPiece;
            capturedPiece.setPosition(capturePos);

            // Add back to active pieces
            if (capturedPiece.getColor() == Color.WHITE) {
                whitePieces.addLast(capturedPiece);
            } else {
                blackPieces.addLast(capturedPiece);
            }
        }

        // Undo castling - move rook back
        if (move.isCastling()) {
            Position rookFrom = move.getRookFrom();
            Position rookTo = move.getRookTo();
            Piece rook = getPieceAt(rookTo);

            board[rookTo.getRow()][rookTo.getCol()] = null;
            board[rookFrom.getRow()][rookFrom.getCol()] = rook;
            rook.setPosition(rookFrom);
            rook.setHasMoved(false);
        }
    }

    /**
     * Redo a move (reapply it)
     */
    public void redoMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPiece();
        Piece capturedPiece = move.getCapturedPiece();

        // Handle en passant
        if (move.isEnPassant()) {
            int capturedPawnRow = piece.getColor() == Color.WHITE ? to.getRow() - 1 : to.getRow() + 1;
            board[capturedPawnRow][to.getCol()] = null;
        }

        // Remove captured piece from active list
        if (capturedPiece != null) {
            if (capturedPiece.getColor() == Color.WHITE) {
                whitePieces.remove(capturedPiece);
            } else {
                blackPieces.remove(capturedPiece);
            }
        }

        // Move piece
        board[from.getRow()][from.getCol()] = null;
        board[to.getRow()][to.getCol()] = piece;
        piece.setPosition(to);
        piece.setHasMoved(true);

        // Handle castling
        if (move.isCastling()) {
            Position rookFrom = move.getRookFrom();
            Position rookTo = move.getRookTo();
            Piece rook = getPieceAt(rookFrom);

            board[rookFrom.getRow()][rookFrom.getCol()] = null;
            board[rookTo.getRow()][rookTo.getCol()] = rook;
            rook.setPosition(rookTo);
            rook.setHasMoved(true);
        }

        // Handle promotion
        if (move.isPromotion()) {
            Piece queen = new Piece(PieceType.QUEEN, piece.getColor(), to);
            queen.setHasMoved(true);
            board[to.getRow()][to.getCol()] = queen;

            if (piece.getColor() == Color.WHITE) {
                whitePieces.remove(piece);
                whitePieces.addLast(queen);
            } else {
                blackPieces.remove(piece);
                blackPieces.addLast(queen);
            }
        }
    }

    /**
     * Check if a player has any legal moves
     */
    public boolean hasLegalMoves(Color color) {
        SinglyLinkedList<Piece> pieces = color == Color.WHITE ? whitePieces : blackPieces;
        SinglyNode<Piece> current = pieces.getHead();

        while (current != null) {
            Piece piece = current.getData();
            // Check all possible destination squares
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Position to = new Position(row, col);
                    if (isValidMove(piece.getPosition(), to, color)) {
                        return true;
                    }
                }
            }
            current = current.getNext();
        }

        return false;
    }

    /**
     * Check if the game is checkmate
     */
    public boolean isCheckmate(Color color) {
        return isInCheck(color) && !hasLegalMoves(color);
    }

    /**
     * Check if the game is stalemate
     */
    public boolean isStalemate(Color color) {
        return !isInCheck(color) && !hasLegalMoves(color);
    }

    /**
     * Get the active pieces linked list for a color
     */
    public SinglyLinkedList<Piece> getActivePieces(Color color) {
        return color == Color.WHITE ? whitePieces : blackPieces;
    }

    /**
     * Reset the board to initial state
     */
    public void reset() {
        initializeBoard();
    }

    /**
     * Get board state as 2D representation for display
     */
    public Piece[][] getBoardState() {
        return board;
    }
}
