package com.example.markdownnotes.repository;

import com.example.markdownnotes.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);

    List<Note> findAllByOrderByUpdatedAtDesc();

    List<Note> findByFolderIsNullOrderByUpdatedAtDesc();

    List<Note> findByFolderIdOrderByUpdatedAtDesc(Long folderId);

    List<Note> findByFolderId(Long folderId);
}
