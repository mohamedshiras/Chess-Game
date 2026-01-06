/**
 * Chess Game - Frontend JavaScript
 * 
 * This file handles:
 * - Rendering the chess board
 * - User interaction (click to select/move pieces)
 * - API calls to Java backend
 * - Updating the UI based on game state
 * 
 * NOTE: All game logic and data structures (Linked Lists) are in the Java backend.
 * This file only handles UI and communication with the server.
 */

// API base URL
const API_URL = '';

// Game state (mirrors backend state)
let gameState = null;

// Currently selected square
let selectedSquare = null;

// Unicode symbols for chess pieces
const PIECE_SYMBOLS = {
    'KING': { 'WHITE': '♔', 'BLACK': '♚' },
    'QUEEN': { 'WHITE': '♕', 'BLACK': '♛' },
    'ROOK': { 'WHITE': '♖', 'BLACK': '♜' },
    'BISHOP': { 'WHITE': '♗', 'BLACK': '♝' },
    'KNIGHT': { 'WHITE': '♘', 'BLACK': '♞' },
    'PAWN': { 'WHITE': '♙', 'BLACK': '♟' }
};

/**
 * Initialize the game when page loads
 */
document.addEventListener('DOMContentLoaded', function () {
    // Create the board
    createBoard();

    // Set up button event listeners
    document.getElementById('btn-undo').addEventListener('click', handleUndo);
    document.getElementById('btn-redo').addEventListener('click', handleRedo);
    document.getElementById('btn-replay').addEventListener('click', handleReplayStart);
    document.getElementById('btn-reset').addEventListener('click', handleReset);
    document.getElementById('btn-replay-next').addEventListener('click', handleReplayNext);
    document.getElementById('btn-replay-end').addEventListener('click', handleReplayEnd);

    // Fetch initial game state
    fetchGameState();
});

/**
 * Create the chess board grid
 * Board is displayed with white at the bottom (row 0) and black at the top (row 7)
 */
function createBoard() {
    const boardElement = document.getElementById('chess-board');
    boardElement.innerHTML = '';

    // Create squares from top (row 7) to bottom (row 0) for display
    for (let displayRow = 7; displayRow >= 0; displayRow--) {
        for (let col = 0; col < 8; col++) {
            const square = document.createElement('div');
            square.className = 'square';

            // Determine square color
            const isLightSquare = (displayRow + col) % 2 === 1;
            square.classList.add(isLightSquare ? 'light' : 'dark');

            // Set data attributes for position
            square.dataset.row = displayRow;
            square.dataset.col = col;

            // Add click event
            square.addEventListener('click', handleSquareClick);

            boardElement.appendChild(square);
        }
    }
}

/**
 * Fetch current game state from the backend
 */
async function fetchGameState() {
    try {
        const response = await fetch(API_URL + '/api/state');
        if (!response.ok) {
            throw new Error('Failed to fetch game state');
        }
        gameState = await response.json();
        updateUI();
    } catch (error) {
        console.error('Error fetching game state:', error);
        showError('Failed to connect to server. Make sure the Java server is running.');
    }
}

/**
 * Update the entire UI based on game state
 */
function updateUI() {
    if (!gameState) return;

    updateBoard();
    updateTurnIndicator();
    updateGameStatus();
    updateCapturedPieces();
    updateMoveHistory();
    updateButtons();
    updateReplayMode();
}

/**
 * Update the chess board with pieces
 */
function updateBoard() {
    const squares = document.querySelectorAll('.square');

    squares.forEach(square => {
        const row = parseInt(square.dataset.row);
        const col = parseInt(square.dataset.col);

        // Clear existing piece
        square.innerHTML = '';

        // Get piece from game state
        const piece = gameState.board[row][col];

        if (piece) {
            const pieceElement = document.createElement('span');
            pieceElement.className = `piece ${piece.color.toLowerCase()}`;
            pieceElement.textContent = PIECE_SYMBOLS[piece.type][piece.color];
            square.appendChild(pieceElement);
        }

        // Remove highlight classes
        square.classList.remove('selected', 'valid-move', 'valid-capture', 'last-move');
    });

    // Highlight selected square
    if (selectedSquare) {
        const selectedElement = document.querySelector(
            `.square[data-row="${selectedSquare.row}"][data-col="${selectedSquare.col}"]`
        );
        if (selectedElement) {
            selectedElement.classList.add('selected');
        }
    }
}

/**
 * Update the turn indicator
 */
function updateTurnIndicator() {
    const turnIndicator = document.getElementById('turn-indicator');
    const isWhiteTurn = gameState.currentTurn === 'WHITE';

    turnIndicator.textContent = isWhiteTurn ? "White's Turn" : "Black's Turn";
    turnIndicator.className = 'turn-indicator ' + (isWhiteTurn ? 'white-turn' : 'black-turn');

    if (gameState.isReplaying) {
        turnIndicator.textContent = "Replay Mode";
    }
}

/**
 * Update game status (check, checkmate, stalemate)
 */
function updateGameStatus() {
    const statusElement = document.getElementById('game-status');

    if (gameState.isGameOver) {
        statusElement.textContent = gameState.gameResult;
        statusElement.className = 'game-status checkmate';
    } else if (gameState.isInCheck) {
        statusElement.textContent = gameState.currentTurn + ' is in CHECK!';
        statusElement.className = 'game-status check';
    } else {
        statusElement.textContent = '';
        statusElement.className = 'game-status';
    }
}

/**
 * Update captured pieces display
 * These are populated from Singly Linked Lists in the backend
 */
function updateCapturedPieces() {
    // White's captures (black pieces they've taken)
    const whiteCapturedElement = document.getElementById('white-captured');
    whiteCapturedElement.innerHTML = '';

    gameState.whiteCaptured.forEach(piece => {
        const pieceElement = document.createElement('div');
        pieceElement.className = 'captured-piece';
        pieceElement.textContent = PIECE_SYMBOLS[piece.type]['BLACK'];
        whiteCapturedElement.appendChild(pieceElement);
    });

    // Black's captures (white pieces they've taken)
    const blackCapturedElement = document.getElementById('black-captured');
    blackCapturedElement.innerHTML = '';

    gameState.blackCaptured.forEach(piece => {
        const pieceElement = document.createElement('div');
        pieceElement.className = 'captured-piece';
        pieceElement.textContent = PIECE_SYMBOLS[piece.type]['WHITE'];
        blackCapturedElement.appendChild(pieceElement);
    });
}

/**
 * Update move history display
 * This data comes from the Doubly Linked List in the backend
 */
function updateMoveHistory() {
    const historyElement = document.getElementById('move-history');
    historyElement.textContent = gameState.moveHistory || '';
}

/**
 * Update button states
 */
function updateButtons() {
    document.getElementById('btn-undo').disabled = !gameState.canUndo;
    document.getElementById('btn-redo').disabled = !gameState.canRedo;
    document.getElementById('btn-replay').disabled = gameState.moveCount === 0 || gameState.isReplaying;
}

/**
 * Update replay mode UI
 */
function updateReplayMode() {
    const replayControls = document.getElementById('replay-controls');
    const container = document.querySelector('.container');

    if (gameState.isReplaying) {
        replayControls.classList.remove('hidden');
        container.classList.add('replaying');
    } else {
        replayControls.classList.add('hidden');
        container.classList.remove('replaying');
    }
}

/**
 * Handle square click
 */
function handleSquareClick(event) {
    // Don't allow moves during replay
    if (gameState && gameState.isReplaying) {
        return;
    }

    const square = event.currentTarget;
    const row = parseInt(square.dataset.row);
    const col = parseInt(square.dataset.col);

    const clickedPiece = gameState.board[row][col];

    if (selectedSquare) {
        // Second click - try to make a move
        if (row === selectedSquare.row && col === selectedSquare.col) {
            // Clicked same square - deselect
            selectedSquare = null;
            updateBoard();
        } else if (clickedPiece && clickedPiece.color === gameState.currentTurn) {
            // Clicked own piece - select it instead
            selectedSquare = { row, col };
            updateBoard();
        } else {
            // Try to make a move
            makeMove(selectedSquare.row, selectedSquare.col, row, col);
        }
    } else {
        // First click - select piece
        if (clickedPiece && clickedPiece.color === gameState.currentTurn) {
            selectedSquare = { row, col };
            updateBoard();
        }
    }
}

/**
 * Convert row/col to chess notation (e.g., 0,4 -> "e1")
 */
function toNotation(row, col) {
    const file = String.fromCharCode('a'.charCodeAt(0) + col);
    const rank = (row + 1).toString();
    return file + rank;
}

/**
 * Make a move via API
 */
async function makeMove(fromRow, fromCol, toRow, toCol) {
    const from = toNotation(fromRow, fromCol);
    const to = toNotation(toRow, toCol);

    try {
        const response = await fetch(API_URL + '/api/move', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ from, to })
        });

        const result = await response.json();

        if (result.success) {
            gameState = result.state;
            selectedSquare = null;
            updateUI();
        } else {
            // Invalid move - just deselect
            selectedSquare = null;
            updateBoard();
        }
    } catch (error) {
        console.error('Error making move:', error);
        showError('Failed to make move. Check server connection.');
    }
}

/**
 * Handle Undo button click
 * Uses the 'previous' pointer in the Doubly Linked List
 */
async function handleUndo() {
    try {
        const response = await fetch(API_URL + '/api/undo', {
            method: 'POST'
        });

        const result = await response.json();
        gameState = result.state;
        selectedSquare = null;
        updateUI();
    } catch (error) {
        console.error('Error undoing move:', error);
        showError('Failed to undo move.');
    }
}

/**
 * Handle Redo button click
 * Uses the 'next' pointer in the Doubly Linked List
 */
async function handleRedo() {
    try {
        const response = await fetch(API_URL + '/api/redo', {
            method: 'POST'
        });

        const result = await response.json();
        gameState = result.state;
        selectedSquare = null;
        updateUI();
    } catch (error) {
        console.error('Error redoing move:', error);
        showError('Failed to redo move.');
    }
}

/**
 * Handle Replay Start button click
 * Resets to beginning and prepares for step-by-step replay
 */
async function handleReplayStart() {
    try {
        const response = await fetch(API_URL + '/api/replay/start', {
            method: 'POST'
        });

        const result = await response.json();
        gameState = result.state;
        selectedSquare = null;
        updateUI();
    } catch (error) {
        console.error('Error starting replay:', error);
        showError('Failed to start replay.');
    }
}

/**
 * Handle Replay Next button click
 * Traverses forward in the Doubly Linked List
 */
async function handleReplayNext() {
    try {
        const response = await fetch(API_URL + '/api/replay/next', {
            method: 'POST'
        });

        const result = await response.json();
        gameState = result.state;
        updateUI();

        if (!result.hasMore) {
            // Replay finished
        }
    } catch (error) {
        console.error('Error replaying next move:', error);
        showError('Failed to replay next move.');
    }
}

/**
 * Handle Replay End button click
 */
async function handleReplayEnd() {
    try {
        const response = await fetch(API_URL + '/api/replay/end', {
            method: 'POST'
        });

        const result = await response.json();
        gameState = result.state;
        updateUI();
    } catch (error) {
        console.error('Error ending replay:', error);
        showError('Failed to end replay.');
    }
}

/**
 * Handle Reset button click
 * Clears all linked lists and resets the game
 */
async function handleReset() {
    if (!confirm('Are you sure you want to start a new game? All progress will be lost.')) {
        return;
    }

    try {
        const response = await fetch(API_URL + '/api/reset', {
            method: 'POST'
        });

        const result = await response.json();
        gameState = result.state;
        selectedSquare = null;
        updateUI();
    } catch (error) {
        console.error('Error resetting game:', error);
        showError('Failed to reset game.');
    }
}

/**
 * Show error message to user
 */
function showError(message) {
    const statusElement = document.getElementById('game-status');
    statusElement.textContent = message;
    statusElement.className = 'game-status check';

    setTimeout(() => {
        if (gameState) {
            updateGameStatus();
        }
    }, 3000);
}
