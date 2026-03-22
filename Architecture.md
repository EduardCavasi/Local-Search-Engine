# Local File System Search Engine - Architecture Overview

This document describes the overall architectural considerations of the search engine, following the guidelines of the C4 model. Thus, the system is analyzed from 4 perspectives/levels: system context, containers, components, classes.  

## 1. System Context (LEVEL 1)
```mermaid
flowchart TB
    actor((User))
    system[[Search Engine Application]]
    external[(Local File System)]

    actor -->|Uses| system
    system -->|Accesses| external
```
### Primary Actor
The **User** is the primary actor who directly interacts with the Search Engine Application to perform searches and retrieve information.

### System Responsibility

- Recursively traverse directories to discover files.
- Extract and validate file data before storing it.
- Insert and update file metadata in a relational database.
- Perform incremental indexing by detecting and updating only modified files.
- Support efficient multi-word search using full-text search features of the DBMS.
- Provide relevant search results with basic file previews.
- Maintain a well-designed database schema with appropriate data types and indexes.
- Handle runtime configuration (e.g., root directory, file ignore rules, report format).
- Track indexing progress and generate summary reports.
- Gracefully handle edge cases:
    - File permission errors
    - Symbolic link loops
    - Corrupted or unsupported files
    - Database connection timeouts
- Provide robust error handling.

### External Dependencies
1. The system depends on the **Operating System** for file management operations.  
2. The Operating System provides access to the **Local File System**, which stores the documents and data that the application indexes and searches.

## 2. Containers (LEVEL 2)
The system's containers correspond to independently deployable units into which the system can be divided.

```mermaid
flowchart TB
    subgraph machine["User machine"]
        direction TB
        FE[["Desktop Frontend<br/>[Tauri · React]"]]
        BE[["Spring Boot backend<br/>[Apache Tomcat]"]]
        DB[("Relational database<br/>[Postgresql]")]
        FE -->|"HTTP / REST"| BE
        BE -->|"HTTP / REST"| FE
        BE -->|"SQL / JDBC"| DB
    end

    User((User))
    User -->|"Uses"| FE

    external((Local File System))
    BE -->|Accesses| external


    class FE,BE,DB blackBox
    class User blackRound

```

| Container           |                                                                                      Responsibility                                                                                       |
|:--------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Desktop Frontend    | Allows the user to index directories, search files using several search criteria and modify general settings of the search engine without taking care of the inner workings of the system |
| Backend             |                                                       All core logic, including file crawling, indexing, search and database access                                                       |
| Postgresql Database |                                                 Persistent storage of file information. Internal indexing schema used for fast searches.                                                  |
  
## 3. Components (LEVEL 3)

This level breaks the Core logic (Backend) into more specific components.  

```mermaid

flowchart TB
    subgraph backend["Spring Boot backend"]
        direction TB
        DC[["DatabaseAccess"]]
        SC[["SearchComponent"]]
        IC[["IndexingComponent"]]
        SET[["EngineSettings"]]
        IC -->|"SQL queries for CRUD operations"| DC
    end

    DB[("Relational database<br/>[Postgresql]")]
    FS[(Local File System)]
    DC -->|"SQL"| DB
    SC -->|"customizable SQL queries"| DC

    IC -->|"reads"| SET
    IC -->|"read / walk"| FS

    FE[[Frontend]]
    FE -->|"index"| IC
    FE -->|"search"| SC
    FE -->|"modify settings"| SET
    class DC,SC,IC blackBox
    class DB,FS extCyl

```

| Component         |                                                                               Responsibility                                                                               |
|:------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| SearchComponent   |                        Builds SQL queries from user search parameters and uses postgresql full text search capabilities to retrieve file previews.                         |
| IndexingComponent |  Walks recursively through the file system and indexes into the database the files that are not already present or have benn modified/created since last index operation.  |
| EngineSettings    | General setting used by the search engine, which can be set by the user: paths to be ignored, files to be ignored, root directories for search and reporting capabilities. |
| DatabaseAccess    |                                              Creates Postgresql pool of connections and makes CRUD operation on the database.                                              |

## 4. Classes (LEVEL 4)
In these diagrams each component is further broken down into classes.
### 4.1. SEARCH COMPONENT 
```mermaid
flowchart TB
    subgraph machine["SearchComponent"]
        direction TB
        SE[["Search Engine"]]
        QB[["Query Builder"]]
        PB[["Preview Builder"]]
        FP[["File Preview"]]
        SE -->|"uses"| QB
        SE -->|"uses"| PB
        PB -->|"builds"| FP
    end
        
        DB[[Database access]]
        SE -->|"uses"| DB

class SE,QB,PB,FP blackBox
class DB extCyl

```

### 4.2. INDEXING CCOMPONENT
```mermaid
flowchart TB
    subgraph machine["IndexingComponent"]
        direction TB
        FC[["File Crawler"]]
        FP[["File Processor"]]
        FS[["File Saver"]]
        IS[["Indexing Stats"]]
        FC -->|"uses"| FP
        FP -->|"uses"| FS
        FC -->|"update"| IS
        FP -->|"update"| IS
        FS -->|"update"| IS
    end
        DAC[["Database access"]]
        FS -->|"uses"| DAC
        DB[(Relational database)]
        DAC -->|"SQL"| DB

class SE,QB,PB,FP blackBox
class DB extCyl

```

### 4.3. ENGINE SETTING
```mermaid
flowchart TB
    subgraph machine["EngineSettings"]
        direction TB
        ES[["Engine Settings"]]
    end
        
        FC[[Indexing Component]]
        FC -->|"uses"| ES
        
        FR((Frontend))
        FR -->|"modifies"| ES

class SE,QB,PB,FP blackBox
class DB extCyl

```

### 4.4. DATABASE ACCESS
```mermaid
flowchart TB
    subgraph machine["DatabaseAccess"]
        direction TB
        DC[["Database Connection"]]
        REP[["Repositories"]]
        REP -->|"uses"| DC
    end
        
        DB[("Relational Database")]
        DC -->|"transaction + pool of connections"| DB

class SE,QB,PB,FP blackBox
class DB extCyl

```

# Backend architecture overview
The backend of this project is implemented using Java Spring Boot and employs a layered architecture.
```mermaid
flowchart TB
    subgraph machine["Backend"]
        direction TB
        CON[["Controllers"]]
        SE[["SearchEngine"]]
        IE[["IndexingEngine"]]
        REP[["Data Repositories"]]
        MOD[["Models"]]
        DC[["Database Connection"]]
        CON --> SE
        CON --> IE
        SE --> REP
        IE --> REP
        REP --> MOD
        REP --> DC
    end
        
        DB[("Relational Database")]
        DC --> DB

class SE,QB,PB,FP blackBox
class DB extCyl

```