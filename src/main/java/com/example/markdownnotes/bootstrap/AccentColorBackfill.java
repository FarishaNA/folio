package com.example.markdownnotes.bootstrap;

import com.example.markdownnotes.model.Note;
import com.example.markdownnotes.repository.NoteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AccentColorBackfill implements CommandLineRunner {

    private final NoteRepository noteRepository;

    public AccentColorBackfill(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Override
    public void run(String... args) {
        noteRepository.findAll().forEach(note -> {
            if (note.getAccentColor() == null || note.getAccentColor().isBlank()) {
                note.setAccentColor(Note.randomAccentColor());
                noteRepository.save(note);
            }
        });
    }
}
