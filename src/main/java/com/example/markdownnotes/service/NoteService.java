package com.example.markdownnotes.service;

import com.example.markdownnotes.model.Folder;
import com.example.markdownnotes.model.Note;
import com.example.markdownnotes.repository.FolderRepository;
import com.example.markdownnotes.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;

    public NoteService(NoteRepository noteRepository, FolderRepository folderRepository) {
        this.noteRepository = noteRepository;
        this.folderRepository = folderRepository;
    }

    public List<Note> listNotes(Long folderId, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Note> found = noteRepository
                    .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword.trim(), keyword.trim());
            return filterByFolder(found, folderId);
        }
        if (folderId == null) {
            return noteRepository.findAllByOrderByUpdatedAtDesc();
        }
        if (folderId == 0L) {
            return noteRepository.findByFolderIsNullOrderByUpdatedAtDesc();
        }
        return noteRepository.findByFolderIdOrderByUpdatedAtDesc(folderId);
    }

    private List<Note> filterByFolder(List<Note> notes, Long folderId) {
        if (folderId == null) {
            return notes;
        }
        if (folderId == 0L) {
            return notes.stream().filter(n -> n.getFolder() == null).collect(Collectors.toList());
        }
        return notes.stream()
                .filter(n -> n.getFolder() != null && folderId.equals(n.getFolder().getId()))
                .collect(Collectors.toList());
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id).orElse(new Note());
    }

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

    private Folder resolveFolder(Long folderId) {
        if (folderId == null || folderId <= 0) {
            return null;
        }
        return folderRepository.findById(folderId).orElse(null);
    }

    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }
}
