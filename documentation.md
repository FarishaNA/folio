# Markdown Notes Manager - Comprehensive Documentation

Welcome to the **Markdown Notes Manager** project! This document serves as a detailed guide to understanding the project architecture, dependencies, technologies used, and the overall development workflow. It is designed to be accessible for beginners while providing enough depth for advanced developers.

---

## 1. Project Overview

The **Markdown Notes Manager** is a web-based application built using **Java** and **Spring Boot**. It allows users to seamlessly create, edit, view, delete, and search personal notes documented in Markdown format.

The application heavily focuses on presenting a premium, clean, and highly professional user interface reminiscent of modern note-taking apps like Notion or Obsidian, moving away from amateur "student-generated" visuals.

---

## 2. Technology Stack

### Backend Technologies
*   **Java (JDK 17+)**: The core programming language used.
*   **Spring Boot (3.x)**: The primary framework powering the application, ensuring quick setup and robust functionality.
*   **Spring Web**: Provides features to build web applications (Controllers, Routing).
*   **Spring Data JPA**: Abstraction layer for interacting with the database using Hibernate.
*   **H2 Database (In-Memory)**: A lightweight, fast in-memory database used for development and testing. Data is transient by default but perfect for a standalone project setup without external dependencies.
*   **Thymeleaf**: A server-side Java template engine used to dynamically render HTML content.

### Frontend Technologies
*   **HTML5 & CSS3**: Core web technologies used for structure and styling.
*   **Vanilla JavaScript**: Handles theme switching (Light/Dark mode) and real-time Markdown parsing.
*   **Marked.js**: A high-performance, lightweight markdown parser used on the client-side to dynamically render markdown strings into rich HTML representations.
*   **Google Fonts (Inter & Roboto Mono)**: Professional typography for UI text and code blocks.

---

## 3. Application Architecture & Structure

The project follows the standard **MVC (Model-View-Controller)** design pattern typical in Spring applications.

### File Directory Breakdown
```text
markdownnotes/
├── src/main/java/com/example/markdownnotes/
│   ├── MarkdownnotesApplication.java       <-- Main Spring Boot entry point
│   ├── controller/
│   │   └── NoteController.java             <-- Handles HTTP requests, routes, and Search parameters
│   ├── model/
│   │   └── Note.java                       <-- JPA Entity defining the Note database table
│   ├── repository/
│   │   └── NoteRepository.java             <-- JPA interface for CRUD operations & Search logic
│   └── service/
│       └── NoteService.java                <-- Business logic layer coordinating data access
├── src/main/resources/
│   ├── application.properties              <-- Configuration (Database, Server port, JPA schema)
│   ├── static/css/
│   │   └── style.css                       <-- The massive CSS file handling premium styling & themes
│   └── templates/
│       ├── index.html                      <-- The homepage template showing the note grid
│       └── editor.html                     <-- The editor template containing the split-view writing mode
├── pom.xml                                 <-- Maven dependencies definition
└── documentation.md                        <-- This documentation file
```

---

## 4. Development Workflow & Data Flow

When a user interacts with the application, data flows seamlessly between the client (Browser) and the server (Spring Boot). 

### Creating or Editing a Note
1. **User Action**: The user clicks "New Note" or selects an existing note.
2. **Controller Request**: `NoteController` intercepts the `GET /new` or `GET /edit/{id}` request.
3. **Logic**: `NoteService` is called to fetch existing notes (for the sidebar) and an empty/existing `Note` object.
4. **View Render**: The controller packages this data into a `Model` and returns the `editor.html` template.
5. **Client Rendering**: 
   * The user types in the `textarea`.
   * An input event listener calls `Marked.js`.
   * `Marked.js` instantly compiles the Markdown to HTML and injects it into the preview pane.
6. **Save Action**: The user clicks "Save Note". A `POST /save` request is sent containing the `Note` object.
7. **Database Storage**: `NoteController` -> `NoteService` -> `NoteRepository` saves the Note to the internal H2 database.

### Searching for Notes
1. **User Action**: The user types a keyword into the navigation search bar and presses Enter.
2. **Controller Request**: The browser issues a `GET /` request appending a `?keyword=xyz` query parameter.
3. **Controller Interception**: 
   ```java
   @GetMapping
   public String listNotes(@RequestParam(required = false) String keyword, Model model) {
       // Check if keyword exists...
   }
   ```
4. **Service & Repository Logic**:
   * The controller calls `noteService.searchNotes(keyword)`.
   * The service calls a custom Spring Data JPA method: `findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword)`.
   * This queries the underlying database for matches in either title or content strings while ignoring case sensitivity.
5. **View Update**: The filtered list of notes is added to the model and `index.html` is rendered. If no results exist, an empty state graphic is displayed.

---

## 5. UI/UX Design Philosophies

This application incorporates advanced design methodologies to prevent feeling like a tutorial project:

*   **Glassmorphism & Backdrop Filters**: Navigation bars and cards utilize subtle background blurs (`backdrop-filter`) to integrate cleanly with their backgrounds.
*   **Dual Theme System**: CSS variables (`--bg-surface`, `--text-primary`) are globally scoped in `:root` and easily swapped based on a `[data-theme="dark"]` attribute updated via JavaScript and stored in `localStorage`.
*   **Interactive Micro-Animations**: Buttons and note cards have smooth hovering translations (`transform: translateY(-4px)`) and responsive box-shadows to provide immediate tactile feedback.
*   **Split-View Editor Layout**: Constructed using CSS Grid (`grid-template-columns: 1fr 1fr;`), allowing independent scrolling (`overflow-y: auto;`) for writing and previewing without breaking the main layout.

---

## 6. How to Run Locally

If you are a developer looking to spin up this project on your local machine:

1. **Prerequisites**: Ensure you have Java 17+ installed and Maven (or use the included Maven Wrapper `mvnw`).
2. **Clone/Navigate**: Open a terminal in the root directory (`/markdownnotes`).
3. **Run Application**: Execute the following command:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(On Windows use `mvnw.cmd spring-boot:run`)*
4. **Access Web Portal**: Open your preferred modern browser and navigate to:
   [http://localhost:8082](http://localhost:8082)
5. **Database Access**: You can inspect the pure H2 database at `http://localhost:8082/h2-console` (configured via `src/main/resources/application.properties`).

---
## 7. Upcoming Improvements (Roadmap)

This project is designed to be easy to extend. Planned improvements focus on stronger organization and a more polished note-taking experience:

### Folder & Organization System
* Add “folders/notebooks” to group notes by topic.
* Support moving notes between folders and filtering the notes list by folder.
* Add tags (with optional tag colors) to enable faster discovery and advanced search.
* Store note metadata (e.g., created/updated timestamps) for better navigation and sorting.

### Color Coding Notes
* Allow users to assign a color label to each note (and optionally to folders).
* Display color indicators in the notes list and sidebar for quick visual scanning.
* Use color accents in the editor UI (headers/badges) to make active notes feel distinct.
* Provide a simple, consistent palette so color remains accessible and readable.

---
*Created by Antigravity*
