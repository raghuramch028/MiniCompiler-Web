<div align="center">
  <h1> MiniCompiler & VM-to-MIPS Translator</h1>
  <p>An end-to-end full-stack project combining a custom Java Compiler with a modern Web Interface.</p>

  **[Review the Live Web Demo Here](https://minicompiler-web.onrender.com)**
  <br><br>

  [![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)
  [![Node.js](https://img.shields.io/badge/Node.js-43853D?style=for-the-badge&logo=node.js&logoColor=white)](#)
  [![Express.js](https://img.shields.io/badge/Express.js-000000?style=for-the-badge&logo=express&logoColor=white)](#)
  [![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)](#)
</div>

---

## Overview

This project is a complete compiler toolchain built from the ground up. It implements a fully functioning **Lexer**, **Recursive Descent Parser**, and **Virtual Machine (VM) Code Generator** written entirely in Java. Furthermore, it includes a downstream **VM-to-MIPS Translator** to emulate stack machines at the instruction level.

To make the compiler accessible, a modern, glassmorphic REST API and web application was built using **Node.js, Express, and Vanilla JS** to seamlessly execute the Java backend via spawned child processes.

## Features

- **Custom Language Parsing**: Understands robust syntax (if/else, while loops, binary operators, variables).
- **Stack Machine Architecture**: Complies to an intermediate Stack VM language before executing raw MIPS Assembly.
- **RESTful API**: Exposes `/api/compile` and `/api/translate` for programmatic execution.
- **Modern Interface**: A sleek, dark-mode GUI enabling code submission in the browser.

## Tech Stack

### Backend Logic (Core)
- **Java SE 11+**
- **Syntax Analysis**: Custom Recursive Descent Parser
- **Code Gen**: Abstract Syntax Tree (AST)

### Web Integration
- **Node.js**: Asynchronous `child_process.spawn`
- **Express.js**: Backend Routing & Middlewares
- **CORS**: Secure Cross-Origin configuration

### Frontend
- **Vanilla JavaScript**: Fetch APIs & DOM manipulation
- **CSS3 / HTML5**: CSS Variables, Glassmorphism UI, Responsive Design

## Project Structure

```text
📦 MiniCompiler-Project
 ┣ 📂 src               # Raw Java compilation engine & translator source code
 ┣ 📂 bin               # Auto-generated .class executables (Ignored by Git)
 ┣ 📂 public            # Frontend Web UI (index.html, styles.css, script.js)
 ┣ 📂 node_modules      # Third-party Node.js dependencies (Ignored by Git)
 ┣ 📜 server.js         # The Express API Server logic
 ┣ 📜 .gitignore        # Standard Git ignores tracking
 ┣ 📜 package.json      # NPM scripts and module management
```

## Getting Started

### Prerequisites
1. Ensure you have **[Java JDK](https://www.oracle.com/java/technologies/downloads/)** installed.
2. Ensure you have **[Node.js](https://nodejs.org/)** installed.

### Installation

1. Clone the repository and navigate into the folder.
2. Recompile the Java source files (if editing them):
   ```bash
   javac -d bin src/MiniCompiler.java src/VMToMipsTranslator.java
   ```
3. Install the web server dependencies:
   ```bash
   npm install
   ```

### Running the App Locally

Start the backend server using the secure NPM script:
```bash
npm start
```
*The server will boot up and locally host your application at `http://localhost:3000`.*

---
<p align="center">Made with ❤️ for Elements of Computing Systems</p>
