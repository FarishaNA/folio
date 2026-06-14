package com.example.markdownnotes.controller;

import com.example.markdownnotes.model.Note;
import com.example.markdownnotes.service.FolderService;
import com.example.markdownnotes.service.NoteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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


    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long folderId,
                            @RequestParam(required = false) String keyword,
                            Model model,
                            Principal principal) {
        String username = principal.getName();

        model.addAttribute("greeting", greeting());
        model.addAttribute("username", username);
        model.addAttribute("folders", folderService.getAllFolders(username));
        model.addAttribute("folderId", folderId);
        model.addAttribute("keyword", keyword);

        List<Note> allNotes = noteService.listNotes(username, null, null);

        long totalWords = allNotes.stream()
            .filter(n -> n.getContent() != null && !n.getContent().isBlank())
            .mapToLong(n -> Arrays.stream(n.getContent().trim().split("\\s+"))
                .filter(w -> !w.isEmpty()).count())
            .sum();

        model.addAttribute("totalNotes", allNotes.size());
        model.addAttribute("totalFolders", folderService.getAllFolders(username).size());
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
            model.addAttribute("notes", noteService.listNotes(username, folderId, keyword));
        }

        return "dashboard";
    }

    @GetMapping("/new")
    public String newNoteForm(@RequestParam(required = false) Long folderId,
                              @RequestParam(required = false) String keyword,
                              Model model,
                              Principal principal) {
        String username = principal.getName();
        model.addAttribute("note", new Note());
        model.addAttribute("notes", noteService.listNotes(username, folderId, keyword));
        model.addAttribute("folders", folderService.getAllFolders(username));
        model.addAttribute("selectedFolderId", folderId);
        model.addAttribute("folderId", folderId);
        model.addAttribute("keyword", keyword);
        return "editor";
    }

    @PostMapping("/save")
    public String saveNote(@ModelAttribute("note") Note note,
                           @RequestParam(required = false) Long folderId,
                           Principal principal) {
        noteService.saveNote(note, folderId, principal.getName());
        if (folderId != null && folderId > 0) {
            return "redirect:/dashboard?folderId=" + folderId;
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editNoteForm(@PathVariable Long id,
                               @RequestParam(required = false) Long folderId,
                               @RequestParam(required = false) String keyword,
                               Model model,
                               Principal principal) {
        String username = principal.getName();
        Note note = noteService.getNoteById(id, principal.getName());
        model.addAttribute("note", note);
        model.addAttribute("notes", noteService.listNotes(username, folderId, keyword));
        model.addAttribute("folders", folderService.getAllFolders(username));
        Long selected = note.getFolder() != null ? note.getFolder().getId() : null;
        model.addAttribute("selectedFolderId", selected);
        model.addAttribute("folderId", folderId);
        model.addAttribute("keyword", keyword);
        return "editor";
    }

    @GetMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id, Principal principal) {
        noteService.deleteNote(id, principal.getName());
        return "redirect:/dashboard";
    }

    @PostMapping("/folders")
    public String createFolder(@RequestParam String name, Principal principal) {
        if (name != null && !name.trim().isEmpty()) {
            folderService.createFolder(name.trim(), principal.getName());
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/folders/delete/{id}")
    public String deleteFolder(@PathVariable Long id, Principal principal) {
        folderService.deleteFolder(id, principal.getName());
        return "redirect:/dashboard";
    }

    @PostMapping("/pin/{id}")
    public String pinNote(@PathVariable Long id, Principal principal) {
        Note note = noteService.getNoteById(id, principal.getName());
        note.setPinned(!note.isPinned());
        noteService.saveNote(note, note.getFolder() != null ? note.getFolder().getId() : null, principal.getName());
        return "redirect:/edit/" + id;
    }
}