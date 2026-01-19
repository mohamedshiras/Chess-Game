# ♟️ Chess Game — Java + Data Structures + Web UI

This project implements a playable Chess game using:

- **Java** for backend game logic & move validation
- **Linked Lists** for board state representation
- **OOP Models** for pieces, moves, and positions
- **HTML + CSS + JS** for browser-based UI rendering
- **Server layer** for game execution coordination

> This project focuses on combining **Data Structures + Game Logic + Web UI** into a cohesive learning-oriented implementation.

---

## 🧩 **Core Architecture**

### **1. Data Structures Layer**
Located in:
src/main/java/org/example/datastructures/
Includes:
- `SinglyLinkedList.java`
- `DoublyLinkedList.java`
- `SinglyNode.java`
- `DoublyNode.java`

Purpose:
✔ Represents the Chess board  
✔ Holds piece objects and movement state  
✔ Demonstrates manual DS implementation (not arrays)

---

### **2. Game Logic Layer**
Located in:
src/main/java/org/example/game/
Key files:
- `ChessBoard.java`
- `ChessGame.java`

Responsibilities:
✔ Initialize board  
✔ Validate moves  
✔ Check/checkmate logic  
✔ Turn tracking  

---

### **3. Model Layer**
Located in:
src/main/java/org/example/model/
Models:
- `Piece.java`
- `PieceType.java`
- `Color.java`
- `Move.java`
- `Position.java`

These represent core chess concepts:
✔ piece identity  
✔ type behavior (rook, knight, pawn…)  
✔ move rules and vector logic  
✔ coordinate positions  

---

### **4. Server Layer**
Located in:
src/main/java/org/example/server/
Contains:
- `ChessServer.java`
- `Main.java`

Use:
✔ Manages execution loop  
✔ Binds game logic to UI or console  

---

### **5. Web UI Layer**
Located in:
src/main/resources/static/
Includes:
css/style.css
js/chess.js
index.html
Purpose:
✔ Render board on browser  
✔ Handle click inputs  
✔ Display piece movements  
✔ Communicate with backend game logic  

---

## 🧠 **Why Linked Lists for Chess?**

Chess boards are traditionally stored as 2D arrays, but this implementation uses **Linked Lists** to emphasize:

✔ Node traversal  
✔ Dynamic insertion  
✔ Pointer manipulation  
✔ Low-level DS understanding

Educational advantages:
> good for students in **Algorithms + OOP + DS courses**

---

## ▶️ **Running the Project**

### **Backend (Java)**
Compile:
```bash
javac -d bin src/main/java/org/example/**/*.java
java -cp bin org.example.server.Main
index.html
python -m http.server 8000
