# Folio

A personal markdown workspace — write, organize, and export notes with a clean dark UI.

**[Live Demo](https://springboot-markdown-notes-production.up.railway.app)**

---

## Features

- **Markdown editor** — live split-view preview with formatting toolbar
- **User authentication** — register, login, logout with BCrypt password hashing
- **Per-user isolation** — notes and folders are private to each account
- **Folder organization** — create folders, assign notes, filter by folder
- **Note pinning** — pin important notes to dashboard
- **Export** — download notes as PDF, HTML, `.md`, or copy to clipboard
- **Dashboard** — stats, quick access grid, recent + pinned notes panels
- **Search** — full-text search across title and content
- **Dark / light theme** — toggle via UI, persisted in localStorage
- **Profile page** — change password, delete account
- **Legal** — Privacy Policy and Terms of Service pages

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.x |
| Security | Spring Security 7 + BCrypt |
| Web / Routing | Spring Web MVC |
| Templating | Thymeleaf |
| ORM | Spring Data JPA (Hibernate) |
| Database | PostgreSQL |
| Frontend | Vanilla JS, Marked.js |
| Deploy | Railway |

---

## Project Structure

markdownnotes/

├── src/main/java/com/example/markdownnotes/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── PasswordConfig.java
│   ├── controller/
│   │   ├── NoteController.java
│   │   ├── AuthController.java
│   │   └── ProfileController.java
│   ├── model/
│   │   ├── Note.java
│   │   ├── Folder.java
│   │   └── User.java
│   ├── repository/
│   │   ├── NoteRepository.java
│   │   ├── FolderRepository.java
│   │   └── UserRepository.java
│   └── service/
│       ├── NoteService.java
│       ├── FolderService.java
│       └── UserService.java
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/style.css
│   └── templates/
│       ├── landing.html
│       ├── dashboard.html
│       ├── editor.html
│       ├── login.html
│       ├── register.html
│       ├── profile.html
│       ├── privacy.html
│       └── tos.html
└── pom.xml

---

## Run Locally

**Prerequisites:** Java 17+, PostgreSQL

**1. Create database:**
```sql
CREATE DATABASE markdownnotes;
```

**2. Set environment variable:**

DATABASE_URL=jdbc:postgresql://localhost:5432/markdownnotes?user=postgres&password=yourpassword

**3. Run:**
```bash
# Windows
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

**4. Open:** `http://localhost:8082`

---

## Routes

| Method | URL | Description |
|---|---|---|
| GET | `/` | Landing page |
| GET | `/register` | Register |
| GET | `/login` | Login |
| GET | `/dashboard` | Dashboard |
| GET | `/new` | New note |
| GET | `/edit/{id}` | Edit note |
| POST | `/save` | Save note |
| GET | `/delete/{id}` | Delete note |
| POST | `/folders` | Create folder |
| GET | `/folders/delete/{id}` | Delete folder |
| POST | `/pin/{id}` | Pin/unpin note |
| GET | `/profile` | Profile page |
| POST | `/profile/change-password` | Change password |
| POST | `/profile/delete-account` | Delete account |
| GET | `/privacy` | Privacy Policy |
| GET | `/tos` | Terms of Service |

---

## Releases

| Version | Description |
|---|---|
| `v0.2.0` | Auth, per-user isolation, profile, legal pages |
| `v0.1.0` | No-auth version — open access |

---

Built by [FarishaNA](https://github.com/FarishaNA)
