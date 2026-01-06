package org.example.datastructures;

/**
 * SinglyNode - A node for Singly Linked List
 * 
 * PURPOSE: This node class is used in Singly Linked Lists to store:
 * - Captured pieces for each player (White and Black separately)
 * - Active pieces on the board for each player
 * 
 * Each node contains data and a reference to the next node only.
 * This is sufficient for captured/active pieces as we only need forward
 * traversal.
 * 
 * @param <T> The type of data stored in this node
 */
public class SinglyNode<T> {

    // The data stored in this node (e.g., a captured piece)
    private T data;

    // Reference to the next node in the list
    private SinglyNode<T> next;

    /**
     * Constructor to create a new node with data
     * 
     * @param data The data to store in this node
     */
    public SinglyNode(T data) {
        this.data = data;
        this.next = null;
    }

    // Getters and Setters

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public SinglyNode<T> getNext() {
        return next;
    }

    public void setNext(SinglyNode<T> next) {
        this.next = next;
    }
}
