package com.example.markdownnotes.controller;

import com.example.markdownnotes.model.Note;
import com.example.markdownnotes.service.FolderService;
import com.example.markdownnotes.service.NoteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class NoteController {

    private final NoteService noteService;
    private final FolderService folderService;

    public NoteController(NoteService noteService, FolderService folderService) {
        this.noteService = noteService;
        this.folderService = folderService;
    }

    @GetMapping
    public String listNotes(@RequestParam(required = false) Long folderId,
                            @RequestParam(required = false) String keyword,
                            Model model) {
        model.addAttribute("notes", noteService.listNotes(folderId, keyword));
        model.addAttribute("folders", folderService.getAllFolders());
        model.addAttribute("folderId", folderId);
        model.addAttribute("keyword", keyword);
        return "index";
    }

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

    @PostMapping("/save")
    public String saveNote(@ModelAttribute("note") Note note,
                           @RequestParam(required = false) Long folderId) {
        noteService.saveNote(note, folderId);
        if (folderId != null && folderId > 0) {
            return "redirect:/?folderId=" + folderId;
        }
        return "redirect:/";
    }

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

    @GetMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return "redirect:/";
    }

    @PostMapping("/folders")
    public String createFolder(@RequestParam String name) {
        if (name != null && !name.trim().isEmpty()) {
            folderService.createFolder(name.trim());
        }
        return "redirect:/";
    }

    @GetMapping("/folders/delete/{id}")
    public String deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return "redirect:/";
    }
}
