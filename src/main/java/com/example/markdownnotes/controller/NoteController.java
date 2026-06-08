package com.example.markdownnotes.controller;

import com.example.markdownnotes.model.Note;
import com.example.markdownnotes.service.FolderService;
import com.example.markdownnotes.service.NoteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class NoteController {

    private final NoteService noteService;
    private final FolderService folderService;

    public NoteController(NoteService noteService, FolderService folderService) {
        this.noteService = noteService;
        this.folderService = folderService;
    }

    private String greeting() {
        int hour = LocalTime.now().getHour();
        return hour < 12 ? "morning" : hour < 17 ? "afternoon" : "evening";
    }

    // ── Landing page ──────────────────────────────────────────
    @GetMapping("/")
    public String home() {
        return "landing";
    }

    // ── Dashboard ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long folderId,
                            @RequestParam(required = false) String keyword,
                            Model model) {

        model.addAttribute("greeting", greeting());
        model.addAttribute("folders", folderService.getAllFolders());
        model.addAttribute("folderId", folderId);
        model.addAttribute("keyword", keyword);

        List<Note> allNotes = noteService.listNotes(null, null);

        long totalWords = allNotes.stream()
            .filter(n -> n.getContent() != null && !n.getContent().isBlank())
            .mapToLong(n -> Arrays.stream(n.getContent().trim().split("\\s+"))
                .filter(w -> !w.isEmpty()).count())
            .sum();

        model.addAttribute("totalNotes", allNotes.size());
        model.addAttribute("totalFolders", folderService.getAllFolders().size());
        model.addAttribute("totalWords", totalWords);

        List<Note> recentNotes = allNotes.stream()
            .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
            .limit(6)
            .toList();
        model.addAttribute("recentNotes", recentNotes);

        List<Note> pinnedNotes = allNotes.stream()
            .filter(Note::isPinned)
            .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
            .toList();
        model.addAttribute("pinnedNotes", pinnedNotes);

        if (keyword != null || folderId != null) {
            model.addAttribute("notes", noteService.listNotes(folderId, keyword));
        }

        return "index";
    }

    // ── New note ──────────────────────────────────────────────
    @GetMapping("/new")
    public String newNoteForm(@RequestParam(required = false) Long folderId,
                              @RequestParam(required = false) String keyword,
                              Model model) {
        model.addAttribute("note", new Note());
        model.addAttribute("notes", noteService.listNotes(folderId, keyword));
        model.addAttribute("folders", folderService.getAllFolders());
        model.addAttribute("selectedFolderId", folderId);
        model.addAttribute("folderId", folderId);
        model.addAttribute("keyword", keyword);
        return "editor";
    }

    // ── Save note ─────────────────────────────────────────────
    @PostMapping("/save")
    public String saveNote(@ModelAttribute("note") Note note,
                           @RequestParam(required = false) Long folderId) {
        noteService.saveNote(note, folderId);
        if (folderId != null && folderId > 0) {
            return "redirect:/dashboard?folderId=" + folderId;
        }
        return "redirect:/dashboard";
    }

    // ── Edit note ─────────────────────────────────────────────
    @GetMapping("/edit/{id}")
    public String editNoteForm(@PathVariable Long id,
                               @RequestParam(required = false) Long folderId,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        Note note = noteService.getNoteById(id);
        model.addAttribute("note", note);
        model.addAttribute("notes", noteService.listNotes(folderId, keyword));
        model.addAttribute("folders", folderService.getAllFolders());
        Long selected = note.getFolder() != null ? note.getFolder().getId() : null;
        model.addAttribute("selectedFolderId", selected);
        model.addAttribute("folderId", folderId);
        model.addAttribute("keyword", keyword);
        return "editor";
    }

    // ── Delete note ───────────────────────────────────────────
    @GetMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return "redirect:/dashboard";
    }

    // ── Create folder ─────────────────────────────────────────
    @PostMapping("/folders")
    public String createFolder(@RequestParam String name) {
        if (name != null && !name.trim().isEmpty()) {
            folderService.createFolder(name.trim());
        }
        return "redirect:/dashboard";
    }

    // ── Delete folder ─────────────────────────────────────────
    @GetMapping("/folders/delete/{id}")
    public String deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return "redirect:/dashboard";
    }

    // ── Pin / unpin ───────────────────────────────────────────
    @PostMapping("/pin/{id}")
    public String pinNote(@PathVariable Long id) {
        Note note = noteService.getNoteById(id);
        note.setPinned(!note.isPinned());
        noteService.saveNote(note, note.getFolder() != null ? note.getFolder().getId() : null);
        return "redirect:/edit/" + id;
    }
}