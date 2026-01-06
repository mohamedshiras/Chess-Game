package org.example;

import org.example.server.ChessServer;

/**
 * Main - Entry point for the Chess Game application
 * 
 * This application demonstrates the use of custom Linked List data structures
 * for a chess game implementation:
 * 
 * - DOUBLY LINKED LIST: Stores move history for Undo/Redo/Replay
 * - SINGLY LINKED LISTS: Store captured pieces and active pieces
 * 
 * NO DATABASE is used - all data exists only in runtime memory.
 * 
 * To run:
 * 1. Compile and run this class
 * 2. Open browser to http://localhost:8080
 */
public class Main {

    public static void main(String[] args) {
        // Default port
        int port = 8080;

        // Allow port to be specified via command line
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number, using default: " + port);
            }
        }

        try {
            // Create and start the chess server
            ChessServer server = new ChessServer(port);
            server.start();

            System.out.println("===========================================");
            System.out.println("   Chess Game - Linked List Implementation");
            System.out.println("===========================================");
            System.out.println();
            System.out.println("Data Structures Used:");
            System.out.println("  - Doubly Linked List: Move History (Undo/Redo/Replay)");
            System.out.println("  - Singly Linked List: Captured Pieces");
            System.out.println("  - Singly Linked List: Active Pieces");
            System.out.println();
            System.out.println("No database - All data in memory only!");
            System.out.println();
            System.out.println("Open your browser and navigate to:");
            System.out.println("  http://localhost:" + port);
            System.out.println();
            System.out.println("Press Ctrl+C to stop the server.");

            // Keep the main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}