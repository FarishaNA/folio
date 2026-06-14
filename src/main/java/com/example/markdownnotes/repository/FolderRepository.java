package com.example.markdownnotes.repository;

import com.example.markdownnotes.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByUserUsernameOrderByNameAsc(String username);
    void deleteAllByUserUsername(String username);
}