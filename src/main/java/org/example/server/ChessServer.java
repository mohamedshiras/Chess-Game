package org.example.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.example.datastructures.DoublyNode;
import org.example.datastructures.SinglyNode;
import org.example.game.ChessGame;
import org.example.model.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ChessServer - HTTP Server for the Chess Game
 * 
 * Provides REST API endpoints for:
 * - GET /api/state - Get current game state
 * - POST /api/move - Make a move
 * - POST /api/undo - Undo last move
 * - POST /api/redo - Redo move
 * - POST /api/replay/start - Start replay mode
 * - POST /api/replay/next - Replay next move
 * - POST /api/replay/end - End replay (go to final position)
 * - POST /api/reset - Reset the game
 * 
 * Also serves static files (HTML, CSS, JS) from the resources folder
 */
public class ChessServer {

    private HttpServer server;
    private ChessGame game;
    private int port;

    public ChessServer(int port) {
        this.port = port;
        this.game = new ChessGame();
    }

    /**
     * Start the HTTP server
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API endpoints
        server.createContext("/api/state", new GameStateHandler());
        server.createContext("/api/move", new MoveHandler());
        server.createContext("/api/undo", new UndoHandler());
        server.createContext("/api/redo", new RedoHandler());
        server.createContext("/api/replay/start", new ReplayStartHandler());
        server.createContext("/api/replay/next", new ReplayNextHandler());
        server.createContext("/api/replay/end", new ReplayEndHandler());
        server.createContext("/api/reset", new ResetHandler());

        // Static file serving
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("Chess Server started on http://localhost:" + port);
        System.out.println("Open your browser and go to http://localhost:" + port);
    }

    /**
     * Stop the server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Convert game state to JSON
     */
    private String getGameStateJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Board state
        json.append("\"board\":[");
        Piece[][] boardState = game.getBoard().getBoardState();
        for (int row = 0; row < 8; row++) {
            json.append("[");
            for (int col = 0; col < 8; col++) {
                Piece piece = boardState[row][col];
                if (piece == null) {
                    json.append("null");
                } else {
                    json.append("{");
                    json.append("\"type\":\"" + piece.getType().name() + "\",");
                    json.append("\"color\":\"" + piece.getColor().name() + "\",");
                    json.append("\"symbol\":\"" + piece.getSymbol() + "\"");
                    json.append("}");
                }
                if (col < 7)
                    json.append(",");
            }
            json.append("]");
            if (row < 7)
                json.append(",");
        }
        json.append("],");

        // Current turn
        json.append("\"currentTurn\":\"" + game.getCurrentTurn().name() + "\",");

        // Game status
        json.append("\"isGameOver\":" + game.isGameOver() + ",");
        json.append(
                "\"gameResult\":" + (game.getGameResult() == null ? "null" : "\"" + game.getGameResult() + "\"") + ",");
        json.append("\"isInCheck\":" + game.isInCheck() + ",");
        json.append("\"isReplaying\":" + game.isReplaying() + ",");

        // Undo/Redo availability
        json.append("\"canUndo\":" + game.canUndo() + ",");
        json.append("\"canRedo\":" + game.canRedo() + ",");

        // Move count
        json.append("\"moveCount\":" + game.getMoveCount() + ",");

        // Move history string
        json.append("\"moveHistory\":\"" + escapeJson(game.getMoveHistoryString()) + "\",");

        /**
         * WHITE CAPTURED PIECES - From Singly Linked List
         * Traverses the linked list to get all captured pieces
         */
        json.append("\"whiteCaptured\":[");
        SinglyNode<Piece> whiteNode = game.getCapturedPieces(Color.WHITE).getHead();
        boolean first = true;
        while (whiteNode != null) {
            if (!first)
                json.append(",");
            Piece p = whiteNode.getData();
            json.append("{\"type\":\"" + p.getType().name() + "\",\"symbol\":\"" + p.getSymbol() + "\"}");
            first = false;
            whiteNode = whiteNode.getNext();
        }
        json.append("],");

        /**
         * BLACK CAPTURED PIECES - From Singly Linked List
         * Traverses the linked list to get all captured pieces
         */
        json.append("\"blackCaptured\":[");
        SinglyNode<Piece> blackNode = game.getCapturedPieces(Color.BLACK).getHead();
        first = true;
        while (blackNode != null) {
            if (!first)
                json.append(",");
            Piece p = blackNode.getData();
            json.append("{\"type\":\"" + p.getType().name() + "\",\"symbol\":\"" + p.getSymbol() + "\"}");
            first = false;
            blackNode = blackNode.getNext();
        }
        json.append("]");

        json.append("}");
        return json.toString();
    }

    /**
     * Escape special characters in JSON strings
     */
    private String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Send JSON response
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    /**
     * Read request body
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }

    /**
     * Parse simple JSON to get a value
     */
    private String getJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIdx = json.indexOf(searchKey);
        if (startIdx == -1)
            return null;

        startIdx += searchKey.length();
        // Skip whitespace
        while (startIdx < json.length() && Character.isWhitespace(json.charAt(startIdx))) {
            startIdx++;
        }

        if (startIdx >= json.length())
            return null;

        char firstChar = json.charAt(startIdx);
        if (firstChar == '"') {
            // String value
            int endIdx = json.indexOf('"', startIdx + 1);
            if (endIdx == -1)
                return null;
            return json.substring(startIdx + 1, endIdx);
        } else {
            // Number or boolean
            int endIdx = startIdx;
            while (endIdx < json.length() &&
                    !Character.isWhitespace(json.charAt(endIdx)) &&
                    json.charAt(endIdx) != ',' &&
                    json.charAt(endIdx) != '}') {
                endIdx++;
            }
            return json.substring(startIdx, endIdx);
        }
    }

    // ============ HTTP Handlers ============

    /**
     * Handler for GET /api/state
     */
    class GameStateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 200, getGameStateJson());
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * Handler for POST /api/move
     */
    class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                String from = getJsonValue(body, "from");
                String to = getJsonValue(body, "to");

                if (from == null || to == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing from or to\"}");
                    return;
                }

                boolean success = game.makeMove(from, to);

                String response = "{\"success\":" + success + ",\"state\":" + getGameStateJson() + "}";
                sendJsonResponse(exchange, 200, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * Handler for POST /api/undo
     */
    class UndoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                boolean success = game.undo();
                String response = "{\"success\":" + success + ",\"state\":" + getGameStateJson() + "}";
                sendJsonResponse(exchange, 200, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * Handler for POST /api/redo
     */
    class RedoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                boolean success = game.redo();
                String response = "{\"success\":" + success + ",\"state\":" + getGameStateJson() + "}";
                sendJsonResponse(exchange, 200, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * Handler for POST /api/replay/start
     */
    class ReplayStartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                game.startReplay();
                String response = "{\"success\":true,\"state\":" + getGameStateJson() + "}";
                sendJsonResponse(exchange, 200, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * Handler for POST /api/replay/next
     */
    class ReplayNextHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                boolean hasMore = game.replayNextMove();
                String response = "{\"success\":true,\"hasMore\":" + hasMore + ",\"state\":" + getGameStateJson() + "}";
                sendJsonResponse(exchange, 200, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * Handler for POST /api/replay/end
     */
    class ReplayEndHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                game.endReplay();
                String response = "{\"success\":true,\"state\":" + getGameStateJson() + "}";
                sendJsonResponse(exchange, 200, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * Handler for POST /api/reset
     */
    class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                game.resetGame();
                String response = "{\"success\":true,\"state\":" + getGameStateJson() + "}";
                sendJsonResponse(exchange, 200, response);
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    /**
     * Handler for static files (HTML, CSS, JS)
     */
    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/")) {
                path = "/index.html";
            }

            // Try to load from resources
            InputStream is = getClass().getResourceAsStream("/static" + path);

            if (is == null) {
                // Try file system as fallback
                Path filePath = Paths.get("src/main/resources/static" + path);
                if (Files.exists(filePath)) {
                    is = new FileInputStream(filePath.toFile());
                }
            }

            if (is == null) {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
                return;
            }

            // Determine content type
            String contentType = "text/plain";
            if (path.endsWith(".html")) {
                contentType = "text/html";
            } else if (path.endsWith(".css")) {
                contentType = "text/css";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript";
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);

            // Read file and send
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            is.close();

            byte[] response = baos.toByteArray();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
    }
}
