package org.example.datastructures;

/**
 * DoublyNode - A node for Doubly Linked List
 * 
 * PURPOSE: This node class is used in Doubly Linked Lists to store:
 * - Move history for the chess game
 * 
 * Each node contains data and references to both next and previous nodes.
 * This bidirectional linking is ESSENTIAL for:
 * - UNDO functionality (traverse backwards using 'previous')
 * - REDO functionality (traverse forwards using 'next')
 * - REPLAY functionality (traverse from head to current position)
 * 
 * @param <T> The type of data stored in this node (typically a Move object)
 */
public class DoublyNode<T> {

    // The data stored in this node (e.g., a chess move)
    private T data;

    // Reference to the next node (for redo/forward traversal)
    private DoublyNode<T> next;

    // Reference to the previous node (for undo/backward traversal)
    private DoublyNode<T> previous;

    /**
     * Constructor to create a new doubly-linked node with data
     * 
     * @param data The data to store in this node
     */
    public DoublyNode(T data) {
        this.data = data;
        this.next = null;
        this.previous = null;
    }

    // Getters and Setters

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public DoublyNode<T> getNext() {
        return next;
    }

    public void setNext(DoublyNode<T> next) {
        this.next = next;
    }

    public DoublyNode<T> getPrevious() {
        return previous;
    }

    public void setPrevious(DoublyNode<T> previous) {
        this.previous = previous;
    }
}
