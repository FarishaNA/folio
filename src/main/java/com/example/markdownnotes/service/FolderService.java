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
import java.util.Optional;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public FolderService(FolderRepository folderRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public List<Folder> getAllFolders(String username) {
        return folderRepository.findByUserUsernameOrderByNameAsc(username);
    }

    public Optional<Folder> getFolder(Long id) {
        return folderRepository.findById(id);
    }

    public Folder createFolder(String name, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Folder folder = new Folder();
        folder.setName(name.trim());
        folder.setUser(user);
        return folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(Long id, String username) {
        Folder folder = folderRepository.findById(id).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!folder.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        List<Note> inFolder = noteRepository.findByUserUsernameAndFolderId(username, id);
        for (Note note : inFolder) {
            note.setFolder(null);
            noteRepository.save(note);
        }
        folderRepository.deleteById(id);
    }
}