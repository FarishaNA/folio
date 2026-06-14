package com.example.markdownnotes.service;

import com.example.markdownnotes.model.Folder;
import com.example.markdownnotes.model.Note;
import com.example.markdownnotes.model.User;
import com.example.markdownnotes.repository.FolderRepository;
import com.example.markdownnotes.repository.NoteRepository;
import com.example.markdownnotes.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, FolderRepository folderRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
    }

    public List<Note> listNotes(String username, Long folderId, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Note> found = noteRepository
                .findByUserUsernameAndTitleContainingIgnoreCaseOrUserUsernameAndContentContainingIgnoreCase(
                    username, keyword.trim(), username, keyword.trim());
            return filterByFolder(found, folderId);
        }
        if (folderId == null) return noteRepository.findByUserUsernameOrderByUpdatedAtDesc(username);
        if (folderId == 0L) return noteRepository.findByUserUsernameAndFolderIsNullOrderByUpdatedAtDesc(username);
        return noteRepository.findByUserUsernameAndFolderIdOrderByUpdatedAtDesc(username, folderId);
    }

    private List<Note> filterByFolder(List<Note> notes, Long folderId) {
        if (folderId == null) return notes;
        if (folderId == 0L) return notes.stream().filter(n -> n.getFolder() == null).collect(Collectors.toList());
        return notes.stream()
            .filter(n -> n.getFolder() != null && folderId.equals(n.getFolder().getId()))
            .collect(Collectors.toList());
    }

    public Note getNoteById(Long id, String username) {
        Note note = noteRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!note.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return note;
    }

    @Transactional
    public void saveNote(Note incoming, Long folderId, String username) {
        Folder folder = resolveFolder(folderId);
        User user = userRepository.findByUsername(username).orElseThrow();

        if (incoming.getId() != null) {
            Note existing = noteRepository.findById(incoming.getId()).orElseThrow();
            existing.setTitle(incoming.getTitle());
            existing.setContent(incoming.getContent());
            existing.setFolder(folder);
            noteRepository.save(existing);
            return;
        }

        incoming.setFolder(folder);
        incoming.setUser(user);
        noteRepository.save(incoming);
    }

    private Folder resolveFolder(Long folderId) {
        if (folderId == null || folderId <= 0) return null;
        return folderRepository.findById(folderId).orElse(null);
    }

    public void deleteNote(Long id, String username) {
        Note note = noteRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!note.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        noteRepository.deleteById(id);
    }
}