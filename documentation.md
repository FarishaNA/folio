# Markdown Notes Manager — Comprehensive Documentation

Welcome to the Markdown Notes Manager project. This document covers the full architecture, technology stack, data flow, and a detailed line-by-line explanation of every change made to add folder organization, accent color coding, and persistent storage.

---

## 1. Project Overview

The Markdown Notes Manager is a web application built with Java and Spring Boot. Users can create, edit, delete, search, and organize personal notes written in Markdown format. The UI is rendered server-side with Thymeleaf and enhanced client-side with Marked.js for live Markdown preview.

The application targets a clean, professional aesthetic similar to modern note-taking tools like Notion or Obsidian, with full light/dark theme support.

---

## 2. Technology Stack

### Backend

| Technology | Role |
|---|---|
| Java 17+ | Core language |
| Spring Boot 3.x | Application framework, auto-configuration |
| Spring Web MVC | HTTP routing, controller layer |
| Spring Data JPA | ORM abstraction over Hibernate |
| H2 Database | Embedded SQL DB; file-based for runtime, in-memory for tests |
| Thymeleaf | Server-side HTML templating |

### Frontend

| Technology | Role |
|---|---|
| HTML5 / CSS3 | Structure and styling |
| Vanilla JavaScript | Theme toggle, live Markdown preview |
| Marked.js | Client-side Markdown → HTML parser |

---

## 3. Project Structure

```
markdownnotes/
├── src/main/java/com/example/markdownnotes/
│   ├── MarkdownnotesApplication.java
│   ├── bootstrap/
│   │   └── AccentColorBackfill.java
│   ├── controller/
│   │   └── NoteController.java
│   ├── model/
│   │   ├── Note.java
│   │   └── Folder.java
│   ├── repository/
│   │   ├── NoteRepository.java
│   │   └── FolderRepository.java
│   └── service/
│       ├── NoteService.java
│       └── FolderService.java
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/style.css
│   └── templates/
│       ├── index.html
│       └── editor.html
├── src/test/resources/
│   └── application.properties
├── data/                        ← gitignored H2 files
└── pom.xml
```

---

## 4. Application Architecture

The application follows the standard Spring MVC layered pattern:

```
Browser  →  NoteController  →  NoteService / FolderService  →  NoteRepository / FolderRepository  →  H2 DB
         ←  Thymeleaf HTML  ←  Model attributes               ←  JPA entities
```

Each layer has a single responsibility:

- **Controller** — maps HTTP verbs and paths to Java methods; builds the Thymeleaf Model; returns view names.
- **Service** — contains all business logic (search filtering, folder resolution, accent color assignment).
- **Repository** — Spring Data JPA interfaces; method names are converted into SQL automatically.
- **Model** — JPA entities that map to database tables.

---

## 5. Data Flow

### Viewing notes

1. Browser sends `GET /` (optionally with `?folderId=` and `?keyword=`).
2. `NoteController.listNotes()` calls `NoteService.listNotes(folderId, keyword)`.
3. `NoteService` chooses the correct repository method:
   - No folder, no keyword → `findAllByOrderByUpdatedAtDesc()`
   - `folderId = 0` → `findByFolderIsNullOrderByUpdatedAtDesc()` (Uncategorized)
   - `folderId = N` → `findByFolderIdOrderByUpdatedAtDesc(N)`
   - keyword present → `findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase()` then filtered in memory by folder
4. Controller adds `notes`, `folders`, `folderId`, and `keyword` to the Model.
5. Thymeleaf renders `index.html` with those values.

### Saving a note

1. Browser sends `POST /save` with the note form data and a `folderId` parameter.
2. `NoteController.saveNote()` passes both to `NoteService.saveNote(note, folderId)`.
3. `NoteService.resolveFolder(folderId)` looks up the Folder entity (returns `null` if `folderId <= 0`).
4. For existing notes the service fetches the DB record and updates title, content, and folder in place.
5. For new notes the service sets the folder on the incoming entity then persists it.
6. On save, the controller redirects to `/?folderId=N` if a folder was active, otherwise to `/`.

---

## 6. What Was Added — Feature-by-Feature

The three features added to the original project were:

1. Folder organization
2. Accent color coding
3. Persistent H2 file storage

The sections below explain every file that was created or changed, with line-level detail.

---

## 6.1 New File: `model/Folder.java`

This file was created from scratch. It defines the `folders` database table.

```java
@Entity
@Table(name = "folders")
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY)
    private List<Note> notes = new ArrayList<>();
    // getters and setters
}
```

- `@Entity` tells Hibernate to create a `folders` table.
- `@GeneratedValue(IDENTITY)` means the database auto-increments the primary key.
- `@OneToMany(mappedBy = "folder")` declares the inverse side of the relationship. The `Note` entity owns the foreign key column; Folder just navigates back to its notes.
- `FetchType.LAZY` means Hibernate does not load the notes list unless it is explicitly accessed.

---

## 6.2 Modified File: `model/Note.java`

Two fields were added to the existing `Note` entity.

**Added field: `folder`**

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "folder_id")
private Folder folder;
```

- `@ManyToOne` — many notes can belong to one folder.
- `@JoinColumn(name = "folder_id")` — Hibernate creates a `folder_id` column in the `notes` table that holds the foreign key reference to `folders.id`.
- `FetchType.LAZY` — the Folder is not loaded from the DB unless `note.getFolder()` is called.
- The field is nullable (no `nullable = false`), so a note with no folder is valid — these become "Uncategorized".

**Added field: `accentColor`**

```java
private static final String[] ACCENT_PALETTE = {
    "#e57373", "#f06292", "#ba68c8", "#9575cd", "#7986cb",
    "#64b5f6", "#4db6ac", "#81c784", "#ffb74d", "#ff8a65"
};

@Column(length = 20)
private String accentColor;

public static String randomAccentColor() {
    return ACCENT_PALETTE[ThreadLocalRandom.current().nextInt(ACCENT_PALETTE.length)];
}
```

- `ACCENT_PALETTE` is a fixed array of ten hex color strings. Having a fixed palette keeps the visual result consistent — random hex values would produce ugly, clashing colors.
- `accentColor` is stored in a `VARCHAR(20)` column.
- `randomAccentColor()` is a static helper used when creating new notes.
- `ThreadLocalRandom` is used instead of `Math.random()` because it is faster in multi-threaded server environments.

---

## 6.3 New File: `repository/FolderRepository.java`

```java
@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByOrderByNameAsc();
}
```

- Extends `JpaRepository<Folder, Long>` to inherit `save()`, `findById()`, `deleteById()`, and others for free.
- `findAllByOrderByNameAsc()` is a Spring Data derived query — no SQL needed; the method name is parsed into `SELECT * FROM folders ORDER BY name ASC`.

---

## 6.4 Modified File: `repository/NoteRepository.java`

Three methods were added to the existing interface.

```java
List<Note> findByFolderIsNullOrderByUpdatedAtDesc();
List<Note> findByFolderIdOrderByUpdatedAtDesc(Long folderId);
List<Note> findByFolderId(Long folderId);
```

- `findByFolderIsNullOrderByUpdatedAtDesc()` — generates `WHERE folder_id IS NULL ORDER BY updated_at DESC`. This powers the "Uncategorized" filter.
- `findByFolderIdOrderByUpdatedAtDesc(Long folderId)` — generates `WHERE folder_id = ? ORDER BY updated_at DESC`. This powers the per-folder filtered view.
- `findByFolderId(Long folderId)` — used by `FolderService.deleteFolder()` to find all notes that need their folder cleared before the folder is deleted.

---

## 6.5 New File: `service/FolderService.java`

This file was created from scratch to handle all folder business logic.

```java
public List<Folder> getAllFolders() {
    return folderRepository.findAllByOrderByNameAsc();
}
```

Returns the alphabetically sorted list of all folders. This is added to the Model on every page load so the sidebar is always populated.

```java
public Folder createFolder(String name) {
    Folder folder = new Folder();
    folder.setName(name.trim());
    return folderRepository.save(folder);
}
```

Trims whitespace before saving so folder names like `"  Work  "` become `"Work"`.

```java
@Transactional
public void deleteFolder(Long id) {
    List<Note> inFolder = noteRepository.findByFolderId(id);
    for (Note note : inFolder) {
        note.setFolder(null);
        noteRepository.save(note);
    }
    folderRepository.deleteById(id);
}
```

- `@Transactional` wraps the entire method in a single database transaction. If the `deleteById` fails after some notes were already un-assigned, the whole operation is rolled back.
- Each note has its `folder` set to `null` (making it Uncategorized) before the folder row is deleted, so no orphaned foreign key references are left.

---

## 6.6 Modified File: `service/NoteService.java`

**Constructor change:** `FolderRepository` was added as a second constructor parameter.

```java
public NoteService(NoteRepository noteRepository, FolderRepository folderRepository) {
    this.noteRepository = noteRepository;
    this.folderRepository = folderRepository;
}
```

Spring injects both repositories automatically because both are `@Repository` beans.

**Modified method: `listNotes(Long folderId, String keyword)`**

```java
public List<Note> listNotes(Long folderId, String keyword) {
    if (keyword != null && !keyword.trim().isEmpty()) {
        List<Note> found = noteRepository
            .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword.trim(), keyword.trim());
        return filterByFolder(found, folderId);
    }
    if (folderId == null)   return noteRepository.findAllByOrderByUpdatedAtDesc();
    if (folderId == 0L)     return noteRepository.findByFolderIsNullOrderByUpdatedAtDesc();
    return noteRepository.findByFolderIdOrderByUpdatedAtDesc(folderId);
}
```

The method now handles three distinct cases in order: keyword search (then filtered in memory by folder), no filter at all (all notes), `folderId = 0` (Uncategorized), and a specific folder id.

**New private method: `filterByFolder()`**

```java
private List<Note> filterByFolder(List<Note> notes, Long folderId) {
    if (folderId == null)  return notes;
    if (folderId == 0L)    return notes.stream()
                               .filter(n -> n.getFolder() == null)
                               .collect(Collectors.toList());
    return notes.stream()
        .filter(n -> n.getFolder() != null && folderId.equals(n.getFolder().getId()))
        .collect(Collectors.toList());
}
```

When a keyword search is active, the full-text search runs first (across all folders), then this method narrows the result to the selected folder in memory using Java streams. This avoids writing a complex combined JPA query.

**Modified method: `saveNote(Note incoming, Long folderId)`**

```java
@Transactional
public void saveNote(Note incoming, Long folderId) {
    Folder folder = resolveFolder(folderId);
    if (incoming.getId() != null) {
        Note existing = noteRepository.findById(incoming.getId()).orElseThrow();
        existing.setTitle(incoming.getTitle());
        existing.setContent(incoming.getContent());
        existing.setFolder(folder);
        noteRepository.save(existing);
        return;
    }
    incoming.setFolder(folder);
    noteRepository.save(incoming);
}
```

For existing notes the method fetches the managed entity from the database and updates it in place, rather than saving the detached form object directly. This is important because the detached form object does not carry the `accentColor` field (it is not part of the form), so saving it directly would overwrite the color with null.

**New private method: `resolveFolder()`**

```java
private Folder resolveFolder(Long folderId) {
    if (folderId == null || folderId <= 0) return null;
    return folderRepository.findById(folderId).orElse(null);
}
```

Centralizes the `folderId → Folder entity` lookup so it does not need to be repeated in multiple places. Returns `null` for "No folder" cases (`folderId` of `null`, `0`, or any negative value), which the JPA layer stores as a `NULL` in the `folder_id` column.

---

## 6.7 Modified File: `controller/NoteController.java`

**Constructor change:** `FolderService` was injected alongside `NoteService`.

```java
public NoteController(NoteService noteService, FolderService folderService) {
    this.noteService = noteService;
    this.folderService = folderService;
}
```

**Modified method: `listNotes()`**

```java
@GetMapping
public String listNotes(@RequestParam(required = false) Long folderId,
                        @RequestParam(required = false) String keyword,
                        Model model) {
    model.addAttribute("notes",   noteService.listNotes(folderId, keyword));
    model.addAttribute("folders", folderService.getAllFolders());
    model.addAttribute("folderId", folderId);
    model.addAttribute("keyword",  keyword);
    return "index";
}
```

`folderId` was added as a new `@RequestParam`. It is passed through to `NoteService` and also added to the Model so Thymeleaf can highlight the active folder in the sidebar.

**Modified method: `saveNote()`**

```java
@PostMapping("/save")
public String saveNote(@ModelAttribute("note") Note note,
                       @RequestParam(required = false) Long folderId) {
    noteService.saveNote(note, folderId);
    if (folderId != null && folderId > 0) return "redirect:/?folderId=" + folderId;
    return "redirect:/";
}
```

After saving, the redirect preserves the active folder context so the user is returned to the same folder view they were in before saving.

**New method: `createFolder()`**

```java
@PostMapping("/folders")
public String createFolder(@RequestParam String name) {
    if (name != null && !name.trim().isEmpty()) {
        folderService.createFolder(name.trim());
    }
    return "redirect:/";
}
```

Accepts the folder name from the sidebar form. The blank-check is done here as a controller guard and again inside `FolderService.createFolder()` as a service-layer guard — defensive duplication in case the service is called from other places in the future.

**New method: `deleteFolder()`**

```java
@GetMapping("/folders/delete/{id}")
public String deleteFolder(@PathVariable Long id) {
    folderService.deleteFolder(id);
    return "redirect:/";
}
```

Uses `@PathVariable` to extract the folder id from the URL. Delegates all logic to `FolderService`.

---

## 6.8 New File: `bootstrap/AccentColorBackfill.java`

```java
@Component
public class AccentColorBackfill implements CommandLineRunner {
    @Override
    public void run(String... args) {
        List<Note> notes = noteRepository.findAll();
        for (Note note : notes) {
            if (note.getAccentColor() == null || note.getAccentColor().isBlank()) {
                note.setAccentColor(Note.randomAccentColor());
                noteRepository.save(note);
            }
        }
    }
}
```

- `CommandLineRunner` is a Spring Boot interface whose `run()` method is called once immediately after the application context starts.
- The loop finds any note that does not have a color assigned (e.g. notes created before the feature existed or after a schema reset) and assigns one from the palette.
- Without this, existing notes would have a `null` `accentColor` and no border on their card — visually inconsistent with newly created notes.

---

## 6.9 Modified File: `application.properties`

```properties
spring.datasource.url=jdbc:h2:file:./data/markdownnotes;AUTO_SERVER=TRUE
spring.jpa.hibernate.ddl-auto=update
```

- `jdbc:h2:file:./data/markdownnotes` — switches from in-memory (`mem:testdb`) to a file-based database. H2 creates `.mv.db` and `.trace.db` files inside the `data/` directory relative to wherever the app is launched.
- `AUTO_SERVER=TRUE` — allows multiple processes (e.g., the running app and the H2 Console) to connect to the same file simultaneously without locking conflicts.
- `ddl-auto=update` — Hibernate compares the current entity schema to the existing database tables and issues `ALTER TABLE` statements to add missing columns (like `folder_id` and `accent_color`) without dropping existing data. It does not remove old columns.

---

## 6.10 New File: `src/test/resources/application.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=create-drop
```

- Placing an `application.properties` in `src/test/resources/` overrides the main one when running tests.
- `mem:testdb` — test runs use an in-memory database, so they are fully isolated from the real data file.
- `DB_CLOSE_DELAY=-1` — keeps the in-memory database alive for the duration of the test JVM session instead of dropping it after the first connection closes.
- `create-drop` — Hibernate creates all tables when the test starts and drops them when the test ends, giving every test run a fresh schema.

---

## 6.11 Modified File: `templates/index.html`

The original layout had a single `<main>` with a flat list of note cards. The revised layout introduces a two-column shell with a permanent folder sidebar on the left.

**Structural change — app shell**

```html
<div class="app-shell">
    <aside class="folders-sidebar"> ... </aside>
    <div class="notes-panel">
        <div class="notes-topbar"> ... </div>
        <div class="notes-content"> ... </div>
    </div>
</div>
```

`app-shell` uses `display: flex; height: 100vh; overflow: hidden` to create a full-viewport two-column layout. The sidebar has a fixed `width: 220px` and the notes panel takes `flex: 1` (all remaining space).

**Folder sidebar items**

```html
<a th:href="@{/(folderId=${f.id})}" class="folder-nav-item"
   th:classappend="${folderId != null and folderId == f.id} ? ' is-active' : ''">
```

The `th:classappend` adds the `is-active` class only when the current `folderId` model attribute matches this folder's id. Thymeleaf evaluates this server-side so no JavaScript is needed.

**Folder delete button — hover-only**

```html
<a th:href="@{/folders/delete/{id}(id=${f.id})}" class="folder-delete-btn"
   onclick="return confirm('Delete this folder? Its notes will become Uncategorized.');">
```

The delete button is a plain `<a>` styled with CSS `opacity: 0` by default, becoming `opacity: 1` only when its parent `.folder-nav-item` is hovered (via `.folder-nav-item:hover .folder-delete-btn`). This replaces the original design that showed all delete controls permanently.

**"New folder" form at the bottom of the sidebar**

```html
<div class="sidebar-footer">
    <form th:action="@{/folders}" method="post" class="folder-create-form">
        <input type="text" name="name" placeholder="New folder…" maxlength="120" class="folder-name-input">
        <button type="submit" class="btn-folder-add"> ... </button>
    </form>
</div>
```

Moved from the main content area into the bottom of the sidebar so it is always accessible without taking space away from the notes grid.

**Removed elements**

The `.folder-toolbar` horizontal pill nav bar and the `.folder-manage-row` (the separate row of folder name + delete button pairs) were completely removed. All folder interaction now lives inside the sidebar.

---

## 6.12 Modified File: `templates/editor.html`

**Folder selector in the toolbar**

```html
<select name="folderId" id="note-folder-select" class="folder-select">
    <option value="" th:selected="${selectedFolderId == null or selectedFolderId == 0}">
        No folder
    </option>
    <option th:each="f : ${folders}" th:value="${f.id}" th:text="${f.name}"
            th:selected="${selectedFolderId != null and selectedFolderId == f.id}">
    </option>
</select>
```

The `<select>` is posted alongside the note form data as `folderId`. `th:selected` on each `<option>` compares the option's folder id to the `selectedFolderId` model attribute, pre-selecting the note's current folder when editing an existing note.

`selectedFolderId` is computed in `NoteController.editNoteForm()` as:

```java
Long selected = note.getFolder() != null ? note.getFolder().getId() : null;
model.addAttribute("selectedFolderId", selected);
```

**Accent dot in the sidebar note list**

```html
<span class="note-accent-dot" th:if="${n.accentColor != null}"
      th:style="|background-color: ${n.accentColor}|"></span>
```

A small colored dot is shown before each note title in the editor sidebar when the note has an accent color assigned.

---

## 7. UI/UX Design Principles

- **Single-axis navigation** — folders live in a left sidebar column; the top bar handles search and new-note action only. No stacked nav bars.
- **Progressive disclosure** — folder delete controls are hidden until hover, reducing visual noise in the normal reading state.
- **Dual theme system** — CSS variables in `:root` and `[data-theme="dark"]` swap the entire color palette. JavaScript reads and writes the `data-theme` attribute on `<html>` and persists the choice in `localStorage`.
- **Split-view editor** — constructed with `display: grid; grid-template-columns: 1fr 1fr`. Each pane has `overflow-y: auto` so they scroll independently.

---

## 8. How to Run Locally

1. Ensure Java 17+ and Maven are installed.
2. Open a terminal in the project root.
3. Run the app:
   ```bash
   ./mvnw spring-boot:run        # macOS / Linux
   .\mvnw.cmd spring-boot:run    # Windows
   ```
4. Open `http://localhost:8082` in a browser.
5. The `data/` directory is created automatically on first run. Do not commit it — it is in `.gitignore`.

---

## 9. Roadmap

The following items are not yet implemented:

- Tag system with optional tag colors for cross-folder discovery
- Sorting options (by title, by creation date)
- Export to plain Markdown file download
- Manual accent color picker in the editor (currently colors are auto-assigned from the palette)
