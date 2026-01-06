package org.example.datastructures;

/**
 * SinglyLinkedList - Custom Singly Linked List Implementation
 * 
 * PURPOSE IN CHESS GAME:
 * 1. Store CAPTURED PIECES for White player - When white captures a black
 * piece,
 * the piece is added to white's captured pieces list
 * 2. Store CAPTURED PIECES for Black player - When black captures a white
 * piece,
 * the piece is added to black's captured pieces list
 * 3. Store ACTIVE PIECES for each player - To efficiently track which pieces
 * are still on the board without scanning all 64 squares
 * 
 * WHY SINGLY LINKED LIST?
 * - We only need forward traversal for displaying captured/active pieces
 * - O(1) insertion at head for adding new captures
 * - O(n) removal when a piece is taken off the active list
 * - Memory efficient: only one pointer per node
 * 
 * NO ARRAYS ARE USED - This is a pure linked list implementation
 * 
 * @param <T> The type of data stored (typically Piece objects)
 */
public class SinglyLinkedList<T> {

    // Head of the list - first node
    private SinglyNode<T> head;

    // Tail of the list - last node (for O(1) append)
    private SinglyNode<T> tail;

    // Number of elements in the list
    private int size;

    /**
     * Constructor - Initialize empty list
     */
    public SinglyLinkedList() {
        this.head = null;
        this.tail = null;
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
     * Add element at the beginning of the list
     * Used when: Adding a newly captured piece (most recent first)
     * Time Complexity: O(1)
     * 
     * @param data The data to add
     */
    public void addFirst(T data) {
        SinglyNode<T> newNode = new SinglyNode<>(data);

        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.setNext(head);
            head = newNode;
        }
        size++;
    }

    /**
     * Add element at the end of the list
     * Used when: Adding active pieces in order
     * Time Complexity: O(1) because we maintain tail reference
     * 
     * @param data The data to add
     */
    public void addLast(T data) {
        SinglyNode<T> newNode = new SinglyNode<>(data);

        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    /**
     * Remove and return the first element
     * Time Complexity: O(1)
     * 
     * @return The removed data, or null if empty
     */
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        T data = head.getData();
        head = head.getNext();

        if (head == null) {
            tail = null;
        }

        size--;
        return data;
    }

    /**
     * Remove a specific element from the list
     * Used when: A piece is captured and needs to be removed from active pieces
     * list
     * Time Complexity: O(n) - need to find the element first
     * 
     * @param data The data to remove
     * @return true if element was found and removed
     */
    public boolean remove(T data) {
        if (isEmpty()) {
            return false;
        }

        // Special case: removing head
        if (head.getData().equals(data)) {
            removeFirst();
            return true;
        }

        // Search for the element
        SinglyNode<T> current = head;
        while (current.getNext() != null) {
            if (current.getNext().getData().equals(data)) {
                // Found it - remove by skipping over it
                if (current.getNext() == tail) {
                    tail = current;
                }
                current.setNext(current.getNext().getNext());
                size--;
                return true;
            }
            current = current.getNext();
        }

        return false;
    }

    /**
     * Check if list contains an element
     * Time Complexity: O(n)
     * 
     * @param data The data to search for
     * @return true if found
     */
    public boolean contains(T data) {
        SinglyNode<T> current = head;
        while (current != null) {
            if (current.getData().equals(data)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    /**
     * Get the head node (for external traversal)
     * 
     * @return head node
     */
    public SinglyNode<T> getHead() {
        return head;
    }

    /**
     * Clear all elements from the list
     * Used when: Resetting the game
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Convert list to string for display
     * Traverses the entire list from head to tail
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        SinglyNode<T> current = head;
        while (current != null) {
            sb.append(current.getData());
            if (current.getNext() != null) {
                sb.append(", ");
            }
            current = current.getNext();
        }
        sb.append("]");
        return sb.toString();
    }
}
