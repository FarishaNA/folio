# Markdown Notes Manager

## Overview
Markdown Notes Manager is a small web application built with **Java + Spring Boot** that lets users create, edit, view, delete, and search **personal notes written in Markdown**. The UI uses **Thymeleaf** on the server side and **client-side Markdown rendering** in the browser.

For deeper architecture and design notes, see [`documentation.md`](./documentation.md).

## Tech Stack
- Java (17+)
- Spring Boot
- Spring Web MVC + Thymeleaf
- Spring Data JPA (with Hibernate)
- H2 Database (in-memory) + H2 Console
- Client-side Markdown rendering (e.g., `marked.js`) + Vanilla JS

## Features (Recruiter-friendly)
- Clean MVC structure (`controller/`, `service/`, `repository/`, `model/`)
- Markdown editor with live preview
- Keyword search across title/content
- Theme support (light/dark) using CSS variables + `localStorage`

## Run Locally
### Prerequisites
- Java 17+
- Maven (or use the provided Maven Wrapper)

### Start the app
Windows:
```powershell
.\mvnw.cmd spring-boot:run
```

macOS/Linux:
```bash
./mvnw spring-boot:run
```

### Open in browser
The app runs on port `8082` by default:
- `http://localhost:8082/`
- Editor: `GET /new`, edit: `GET /edit/{id}`
- H2 Console: `http://localhost:8082/h2-console`

H2 Console (from `src/main/resources/application.properties`):
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: *(blank)*

## Test
```powershell
.\mvnw.cmd test
```

```bash
./mvnw test
```

## Project Structure
- `src/main/java/com/example/markdownnotes/`
  - `controller/` - HTTP routes and MVC view selection
  - `service/` - business logic
  - `repository/` - data access layer (JPA)
  - `model/` - `Note` entity
- `src/main/resources/`
  - `templates/` - `index.html`, `editor.html`
  - `static/` - CSS (and other static assets)
  - `application.properties` - server + H2 configuration

## Screenshots
If you add images later, consider placing them in a `/docs` folder and linking them from here.

