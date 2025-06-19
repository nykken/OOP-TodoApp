package com.example.todo.services;

import com.example.todo.dto.NoteRequest;
import com.example.todo.dto.NoteResponse;
import com.example.todo.entities.Note;
import com.example.todo.repositories.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public Optional<NoteResponse> getNote(Long id) {
        return noteRepository.findById(id)
                .map(ConversionUtils::convertNoteToResponse);
    }

    public List<NoteResponse> getAllNotes() {
        return noteRepository.findAll().stream()
                .map(ConversionUtils::convertNoteToResponse)
                .toList();
    }

    public NoteResponse createNote(NoteRequest request) {
        String title = (request.getTitle() == null || request.getTitle().trim().isEmpty())
                ? "New Note"
                : request.getTitle();

        Note note = new Note(title, request.getBody());
        Note savedNote = noteRepository.save(note);
        return ConversionUtils.convertNoteToResponse(savedNote);
    }

    public Optional<NoteResponse> updateNote(Long id, NoteRequest request) {
        return noteRepository.findById(id)
                .map(note -> {
                    String title = (request.getTitle() == null || request.getTitle().trim().isEmpty())
                            ? "New Note"
                            : request.getTitle();
                    note.setTitle(title);
                    note.setBody(request.getBody());
                    return ConversionUtils.convertNoteToResponse(noteRepository.save(note));
                });
    }

    public boolean deleteNote(Long id) {
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
