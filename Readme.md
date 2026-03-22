# Local File System Search Engine

A desktop-based search engine that indexes and searches files stored on a local machine. The system scans directories, extracts file metadata and content, stores indexed data in a relational database, and provides fast full-text search capabilities through a desktop application interface.

---

## ✨ Features

### Core Features

* Recursive file system traversal
* Metadata and content extraction from files
* Storage of indexed data in a relational database
* Fast single-word and multi-word full-text search
* Ranked search results with file previews

### Advanced Features

* Incremental indexing (only modified files are reprocessed)
* Runtime configuration:

    * Root directory selection
    * File ignore rules
    * Report format selection
* Indexing progress tracking
* Summary report generation

### Reliability & Robustness

* Handles file permission issues gracefully
* Prevents infinite loops from symbolic links
* Manages database connection failures safely
* Prevents crashes from corrupted or unsupported files
* Comprehensive error handling and logging

### Software Quality

* Well-structured modular architecture
* Object-Oriented Programming principles
* Efficient database schema with indexes
* Clean and maintainable codebase
* Consistent coding style
* Meaningful commit messages and logical commit history

---

## 🏗️ System Architecture

The application runs entirely on the user's machine and consists of three main parts:

1. **Frontend Desktop Application**

    * Built using Tauri, React, and Vite
    * Provides user interface
    * Sends HTTP requests to backend services

2. **Backend Service**

    * Built with Spring Boot
    * Runs on an embedded Apache Tomcat server
    * Handles business logic and search operations

3. **Database**

    * PostgreSQL relational database
    * Stores indexed file metadata and content
    * Provides full-text search capabilities


## ⚙️ Configuration Options

The system can be configured at runtime:

* Root directory path
* File type filters
* File ignore patterns
---

## 🧰 Technology Stack

### Frontend

* Tauri
* React
* Vite

### Backend

* Java
* Spring Boot
* Apache Tomcat

### Database

* PostgreSQL

---