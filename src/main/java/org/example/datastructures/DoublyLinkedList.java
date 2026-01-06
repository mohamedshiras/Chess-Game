package org.example.datastructures;

/**
 * DoublyLinkedList - Custom Doubly Linked List Implementation
 * 
 * PURPOSE IN CHESS GAME:
 * Store the MOVE HISTORY of the entire game. Each node contains a Move object
 * that records: from-square, to-square, piece moved, piece captured (if any)
 * 
 * WHY DOUBLY LINKED LIST?
 * - UNDO: Move backwards through history using 'previous' pointers
 * - REDO: Move forwards through history using 'next' pointers
 * - REPLAY: Start from head and traverse to any point
 * 
 * The 'current' pointer tracks where we are in the history:
 * - When a new move is made, it's added after 'current' and becomes the new
 * 'current'
 * - When UNDO is called, 'current' moves to 'previous'
 * - When REDO is called, 'current' moves to 'next'
 * - When a new move is made after UNDO, all future moves (redo history) are
 * discarded
 * 
 * NO ARRAYS ARE USED - This is a pure linked list implementation
 * 
 * @param <T> The type of data stored (typically Move objects)
 */
public class DoublyLinkedList<T> {

    // Head of the list - first move ever made
    private DoublyNode<T> head;

    // Tail of the list - last move in history
    private DoublyNode<T> tail;

    // Current position in history - essential for undo/redo
    private DoublyNode<T> current;

    // Number of elements in the list
    private int size;

    /**
     * Constructor - Initialize empty list
     */
    public DoublyLinkedList() {
        this.head = null;
        this.tail = null;
        this.current = null;
        this.size = 0;
    }

    /**
     * Check if list is empty
     * 
     * @return true if list has no elements
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Get the number of elements in the list
     * 
     * @return size of the list
     */
    public int getSize() {
        return size;
    }

    /**
     * Add a new move to the history
     * This is called every time a chess move is made
     * 
     * IMPORTANT: If we're not at the end of the list (after undo),
     * adding a new move discards all future moves (redo history is lost)
     * 
     * Time Complexity: O(1)
     * 
     * @param data The move data to add
     */
    public void addMove(T data) {
        DoublyNode<T> newNode = new DoublyNode<>(data);

        if (isEmpty()) {
            // First move in the game
            head = newNode;
            tail = newNode;
            current = newNode;
        } else if (current == null) {
            // All moves were undone, start fresh after head
            newNode.setNext(null);
            newNode.setPrevious(null);
            head = newNode;
            tail = newNode;
            current = newNode;
            size = 0; // Reset size, will be incremented below
        } else if (current == tail) {
            // Normal case: adding at the end
            newNode.setPrevious(tail);
            tail.setNext(newNode);
            tail = newNode;
            current = newNode;
        } else {
            // Adding after undo - discard redo history
            // This happens when player undoes, then makes a different move
            newNode.setPrevious(current);
            current.setNext(newNode);
            tail = newNode;
            current = newNode;

            // Recalculate size (count from head to new tail)
            int newSize = 0;
            DoublyNode<T> temp = head;
            while (temp != null) {
                newSize++;
                temp = temp.getNext();
            }
            size = newSize - 1; // Will be incremented below
        }

        size++;
    }

    /**
     * UNDO - Move current pointer backwards
     * Returns the move that was undone so it can be reversed on the board
     * 
     * Time Complexity: O(1)
     * 
     * @return The move to undo, or null if nothing to undo
     */
    public T undo() {
        if (current == null) {
            // Nothing to undo
            return null;
        }

        T undoneMove = current.getData();
        current = current.getPrevious();
        return undoneMove;
    }

    /**
     * REDO - Move current pointer forwards
     * Returns the move to redo so it can be reapplied on the board
     * 
     * Time Complexity: O(1)
     * 
     * @return The move to redo, or null if nothing to redo
     */
    public T redo() {
        DoublyNode<T> nextMove;

        if (current == null && head != null) {
            // We've undone everything, redo the first move
            nextMove = head;
        } else if (current != null && current.getNext() != null) {
            // Normal redo
            nextMove = current.getNext();
        } else {
            // Nothing to redo
            return null;
        }

        current = nextMove;
        return current.getData();
    }

    /**
     * Check if undo is possible
     * 
     * @return true if there are moves to undo
     */
    public boolean canUndo() {
        return current != null;
    }

    /**
     * Check if redo is possible
     * 
     * @return true if there are moves to redo
     */
    public boolean canRedo() {
        if (current == null && head != null) {
            return true;
        }
        return current != null && current.getNext() != null;
    }

    /**
     * Get the head node (for replay from beginning)
     * 
     * @return head node
     */
    public DoublyNode<T> getHead() {
        return head;
    }

    /**
     * Get current position node
     * 
     * @return current node
     */
    public DoublyNode<T> getCurrent() {
        return current;
    }

    /**
     * Reset current to before first move (for replay)
     * After this, calling redo() repeatedly will replay all moves
     */
    public void resetToStart() {
        current = null;
    }

    /**
     * Set current to the end (after replay)
     */
    public void goToEnd() {
        current = tail;
    }

    /**
     * Clear all history
     * Used when: Resetting the game
     */
    public void clear() {
        head = null;
        tail = null;
        current = null;
        size = 0;
    }

    /**
     * Get the number of moves that can be undone
     * (moves from head to current)
     */
    public int getUndoCount() {
        if (current == null) {
            return 0;
        }
        int count = 0;
        DoublyNode<T> temp = head;
        while (temp != null && temp != current.getNext()) {
            count++;
            temp = temp.getNext();
        }
        return count;
    }

    /**
     * Get the number of moves that can be redone
     * (moves from current to tail)
     */
    public int getRedoCount() {
        if (current == null && head != null) {
            // Count all moves
            int count = 0;
            DoublyNode<T> temp = head;
            while (temp != null) {
                count++;
                temp = temp.getNext();
            }
            return count;
        }
        if (current == null) {
            return 0;
        }
        int count = 0;
        DoublyNode<T> temp = current.getNext();
        while (temp != null) {
            count++;
            temp = temp.getNext();
        }
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MoveHistory[");
        DoublyNode<T> temp = head;
        while (temp != null) {
            if (temp == current) {
                sb.append(">>>");
            }
            sb.append(temp.getData());
            if (temp == current) {
                sb.append("<<<");
            }
            if (temp.getNext() != null) {
                sb.append(" <-> ");
            }
            temp = temp.getNext();
        }
        sb.append("]");
        return sb.toString();
    }
}
