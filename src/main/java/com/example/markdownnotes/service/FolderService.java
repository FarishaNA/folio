package com.example.markdownnotes.service;

import com.example.markdownnotes.model.Folder;
import com.example.markdownnotes.model.Note;
import com.example.markdownnotes.repository.FolderRepository;
import com.example.markdownnotes.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final NoteRepository noteRepository;

    public FolderService(FolderRepository folderRepository, NoteRepository noteRepository) {
        this.folderRepository = folderRepository;
        this.noteRepository = noteRepository;
    }

    public List<Folder> getAllFolders() {
        return folderRepository.findAllByOrderByNameAsc();
    }

    public Optional<Folder> getFolder(Long id) {
        return folderRepository.findById(id);
    }

    public Folder createFolder(String name) {
        Folder folder = new Folder();
        folder.setName(name.trim());
        return folderRepository.save(folder);
    }

    @Transactional
    public void deleteFolder(Long id) {
        List<Note> inFolder = noteRepository.findByFolderId(id);
        for (Note note : inFolder) {
            note.setFolder(null);
            noteRepository.save(note);
        }
        folderRepository.deleteById(id);
    }
}
