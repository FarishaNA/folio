package com.example.markdownnotes.repository;

import com.example.markdownnotes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserUsernameOrderByUpdatedAtDesc(String username);
    List<Note> findByUserUsernameAndFolderIsNullOrderByUpdatedAtDesc(String username);
    List<Note> findByUserUsernameAndFolderIdOrderByUpdatedAtDesc(String username, Long folderId);
    List<Note> findByUserUsernameAndFolderId(String username, Long folderId);
    List<Note> findByUserUsernameAndTitleContainingIgnoreCaseOrUserUsernameAndContentContainingIgnoreCase(
        String u1, String title, String u2, String content);
    void deleteAllByUserUsername(String username);
}