package com.example.markdownnotes.service;

import com.example.markdownnotes.model.User;
import com.example.markdownnotes.repository.FolderRepository;
import com.example.markdownnotes.repository.NoteRepository;
import com.example.markdownnotes.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       NoteRepository noteRepository, FolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.noteRepository = noteRepository;
        this.folderRepository = folderRepository;
    }

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username taken");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            List.of()
        );
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String username) {
        noteRepository.deleteAllByUserUsername(username);
        folderRepository.deleteAllByUserUsername(username);
        User user = userRepository.findByUsername(username).orElseThrow();
        userRepository.delete(user);
    }
}