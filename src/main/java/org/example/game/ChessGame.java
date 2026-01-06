package org.example.game;

import org.example.datastructures.DoublyLinkedList;
import org.example.datastructures.DoublyNode;
import org.example.datastructures.SinglyLinkedList;
import org.example.datastructures.SinglyNode;
import org.example.model.*;

/**
 * ChessGame - Main game manager class
 * 
 * THIS CLASS DEMONSTRATES THE CORE LINKED LIST USAGE:
 * 
 * 1. DOUBLY LINKED LIST for Move History:
 * - Stores every move made in the game
 * - Enables UNDO by traversing backwards (previous pointer)
 * - Enables REDO by traversing forwards (next pointer)
 * - Enables REPLAY by starting from head and moving forward
 * 
 * 2. SINGLY LINKED LISTS for Captured Pieces:
 * - whiteCapturedPieces: Pieces captured BY white (black pieces that were
 * taken)
 * - blackCapturedPieces: Pieces captured BY black (white pieces that were
 * taken)
 * - Forward-only traversal is sufficient for displaying captures
 * 
 * NO DATABASE - All data exists only in runtime memory
 * NO ARRAYS for storage - All collections use custom linked list
 * implementations
 */
public class ChessGame {

    // The chess board
    private ChessBoard board;

    // Current turn
    private Color currentTurn;

    /**
     * DOUBLY LINKED LIST: Move History
     * 
     * PURPOSE: Store all moves for undo/redo/replay functionality
     * 
     * Structure: HEAD <-> Move1 <-> Move2 <-> Move3 <-> ... <-> CURRENT <-> ... <->
     * TAIL
     * 
     * - UNDO: current = current.previous, then undo the move on the board
     * - REDO: current = current.next, then redo the move on the board
     * - REPLAY: Reset to start, then repeatedly redo
     */
    private DoublyLinkedList<Move> moveHistory;

    /**
     * SINGLY LINKED LIST: Pieces captured by White
     * Contains black pieces that white has captured
     */
    private SinglyLinkedList<Piece> whiteCapturedPieces;

    /**
     * SINGLY LINKED LIST: Pieces captured by Black
     * Contains white pieces that black has captured
     */
    private SinglyLinkedList<Piece> blackCapturedPieces;

    // Game state
    private boolean gameOver;
    private String gameResult;

    // Replay state
    private boolean isReplaying;

    /**
     * Constructor - Initialize a new game
     */
    public ChessGame() {
        this.board = new ChessBoard();
        this.currentTurn = Color.WHITE;

        // Initialize the DOUBLY LINKED LIST for move history
        this.moveHistory = new DoublyLinkedList<>();

        // Initialize SINGLY LINKED LISTS for captured pieces
        this.whiteCapturedPieces = new SinglyLinkedList<>();
        this.blackCapturedPieces = new SinglyLinkedList<>();

        this.gameOver = false;
        this.gameResult = null;
        this.isReplaying = false;
    }

    /**
     * Make a move on the board
     * 
     * @param fromNotation Starting square (e.g., "e2")
     * @param toNotation   Ending square (e.g., "e4")
     * @return true if move was successful
     */
    public boolean makeMove(String fromNotation, String toNotation) {
        if (gameOver || isReplaying) {
            return false;
        }

        Position from = Position.fromNotation(fromNotation);
        Position to = Position.fromNotation(toNotation);

        if (from == null || to == null) {
            return false;
        }

        return makeMove(from, to);
    }

    /**
     * Make a move using Position objects
     */
    public boolean makeMove(Position from, Position to) {
        if (gameOver || isReplaying) {
            return false;
        }

        // Validate the move
        if (!board.isValidMove(from, to, currentTurn)) {
            return false;
        }

        // Execute the move
        Move move = board.executeMove(from, to);

        /**
         * ADD MOVE TO DOUBLY LINKED LIST
         * This is the key operation for move history tracking
         * 
         * If we've undone moves and make a new move, the redo history is discarded
         * (as in standard chess applications)
         */
        moveHistory.addMove(move);

        // If a piece was captured, add it to the appropriate captured list
        if (move.getCapturedPiece() != null) {
            Piece captured = move.getCapturedPiece();

            /**
             * ADD CAPTURED PIECE TO SINGLY LINKED LIST
             * - If white captured: add to whiteCapturedPieces
             * - If black captured: add to blackCapturedPieces
             */
            if (currentTurn == Color.WHITE) {
                // White captured a black piece
                whiteCapturedPieces.addFirst(captured);
            } else {
                // Black captured a white piece
                blackCapturedPieces.addFirst(captured);
            }
        }

        // Switch turns
        currentTurn = currentTurn.opposite();

        // Check for game end conditions
        checkGameStatus();

        return true;
    }

    /**
     * UNDO - Traverse backwards in the move history doubly linked list
     * 
     * This uses the 'previous' pointer in the DoublyLinkedList
     */
    public boolean undo() {
        if (isReplaying) {
            return false;
        }

        if (!moveHistory.canUndo()) {
            return false;
        }

        // Get the move to undo from the doubly linked list
        Move move = moveHistory.undo();

        if (move == null) {
            return false;
        }

        // Undo the move on the board
        board.undoMove(move);

        // If there was a captured piece, remove it from captured list
        if (move.getCapturedPiece() != null) {
            Piece captured = move.getCapturedPiece();

            /**
             * REMOVE FROM SINGLY LINKED LIST
             * When undoing, we need to remove the piece from captured list
             */
            if (move.getPiece().getColor() == Color.WHITE) {
                whiteCapturedPieces.removeFirst();
            } else {
                blackCapturedPieces.removeFirst();
            }
        }

        // Switch turn back
        currentTurn = currentTurn.opposite();

        // Reset game over status if we're undoing from game end
        gameOver = false;
        gameResult = null;

        return true;
    }

    /**
     * REDO - Traverse forwards in the move history doubly linked list
     * 
     * This uses the 'next' pointer in the DoublyLinkedList
     */
    public boolean redo() {
        if (isReplaying) {
            return false;
        }

        if (!moveHistory.canRedo()) {
            return false;
        }

        // Get the move to redo from the doubly linked list
        Move move = moveHistory.redo();

        if (move == null) {
            return false;
        }

        // Redo the move on the board
        board.redoMove(move);

        // If there was a captured piece, add it back to captured list
        if (move.getCapturedPiece() != null) {
            Piece captured = move.getCapturedPiece();

            if (move.getPiece().getColor() == Color.WHITE) {
                whiteCapturedPieces.addFirst(captured);
            } else {
                blackCapturedPieces.addFirst(captured);
            }
        }

        // Switch turn
        currentTurn = currentTurn.opposite();

        // Check for game end
        checkGameStatus();

        return true;
    }

    /**
     * START REPLAY - Reset to beginning and prepare for step-by-step replay
     * 
     * This resets the board and the doubly linked list current pointer
     */
    public void startReplay() {
        // Reset the board to initial state
        board.reset();
        currentTurn = Color.WHITE;

        // Clear captured pieces lists
        whiteCapturedPieces.clear();
        blackCapturedPieces.clear();

        // Reset the move history to the beginning (but keep all moves)
        moveHistory.resetToStart();

        gameOver = false;
        gameResult = null;
        isReplaying = true;
    }

    /**
     * REPLAY NEXT MOVE - Step forward one move in replay
     * 
     * This traverses forward in the doubly linked list
     */
    public boolean replayNextMove() {
        if (!isReplaying) {
            return false;
        }

        if (!moveHistory.canRedo()) {
            // Replay finished
            isReplaying = false;
            checkGameStatus();
            return false;
        }

        // Get next move from the doubly linked list
        Move move = moveHistory.redo();

        if (move == null) {
            isReplaying = false;
            return false;
        }

        // Execute the move on the board
        board.redoMove(move);

        // Add captured piece to list
        if (move.getCapturedPiece() != null) {
            if (move.getPiece().getColor() == Color.WHITE) {
                whiteCapturedPieces.addFirst(move.getCapturedPiece());
            } else {
                blackCapturedPieces.addFirst(move.getCapturedPiece());
            }
        }

        // Switch turn
        currentTurn = currentTurn.opposite();

        return true;
    }

    /**
     * END REPLAY - Go to the final position
     */
    public void endReplay() {
        while (replayNextMove()) {
            // Continue until all moves are replayed
        }
        isReplaying = false;
        checkGameStatus();
    }

    /**
     * RESET GAME - Start a completely new game
     * 
     * Clears all linked lists and resets the board
     */
    public void resetGame() {
        board.reset();
        currentTurn = Color.WHITE;

        // Clear the move history doubly linked list
        moveHistory.clear();

        // Clear the captured pieces singly linked lists
        whiteCapturedPieces.clear();
        blackCapturedPieces.clear();

        gameOver = false;
        gameResult = null;
        isReplaying = false;
    }

    /**
     * Check if the game has ended (checkmate or stalemate)
     */
    private void checkGameStatus() {
        if (board.isCheckmate(currentTurn)) {
            gameOver = true;
            gameResult = currentTurn.opposite().toString() + " wins by checkmate!";
        } else if (board.isStalemate(currentTurn)) {
            gameOver = true;
            gameResult = "Draw by stalemate!";
        }
    }

    // Getters

    public ChessBoard getBoard() {
        return board;
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getGameResult() {
        return gameResult;
    }

    public boolean isReplaying() {
        return isReplaying;
    }

    /**
     * Check if current player is in check
     */
    public boolean isInCheck() {
        return board.isInCheck(currentTurn);
    }

    /**
     * Get move history linked list
     */
    public DoublyLinkedList<Move> getMoveHistory() {
        return moveHistory;
    }

    /**
     * Get captured pieces for a player (what they captured)
     */
    public SinglyLinkedList<Piece> getCapturedPieces(Color capturer) {
        return capturer == Color.WHITE ? whiteCapturedPieces : blackCapturedPieces;
    }

    /**
     * Check if undo is available
     */
    public boolean canUndo() {
        return moveHistory.canUndo() && !isReplaying;
    }

    /**
     * Check if redo is available
     */
    public boolean canRedo() {
        return moveHistory.canRedo() && !isReplaying;
    }

    /**
     * Get the number of moves played
     */
    public int getMoveCount() {
        return moveHistory.getSize();
    }

    /**
     * Get move history as a string (for display)
     * Traverses the doubly linked list from head
     */
    public String getMoveHistoryString() {
        StringBuilder sb = new StringBuilder();
        DoublyNode<Move> node = moveHistory.getHead();
        int moveNum = 1;

        while (node != null) {
            if (moveNum % 2 == 1) {
                sb.append(((moveNum + 1) / 2) + ". ");
            }
            sb.append(node.getData().getNotation() + " ");
            node = node.getNext();
            moveNum++;
        }

        return sb.toString().trim();
    }
}
