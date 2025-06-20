package com.example.todo.services;

import com.example.todo.dto.NoteRequest;
import com.example.todo.dto.NoteResponse;
import com.example.todo.entities.Note;
import com.example.todo.repositories.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository noteRepository;

    /**
     * Retrieve a note by ID.
     *
     * @param id the note ID
     * @return the note if found, empty otherwise
     */
    public Optional<NoteResponse> getNote(Long id) {
        return noteRepository.findById(id)
                .map(ConversionUtils::convertNoteToResponse);
    }

    /**
     * Retrieve all notes.
     *
     * @return list of all notes
     */
    public List<NoteResponse> getAllNotes() {
        return noteRepository.findAll().stream()
                .map(ConversionUtils::convertNoteToResponse)
                .toList();
    }

    /**
     * Create a new note.
     * Set default title if none provided.
     *
     * @param request the note data
     * @return the created note
     */
    @Transactional
    public NoteResponse createNote(NoteRequest request) {
        String title = (request.getTitle() == null || request.getTitle().trim().isEmpty())
                ? "New Note"
                : request.getTitle();

        Note note = new Note(title, request.getBody());
        Note savedNote = noteRepository.save(note);
        return ConversionUtils.convertNoteToResponse(savedNote);
    }

    /**
     * Update an existing note.
     *
     * @param id the note ID
     * @param request the updated note data
     * @return the updated note if found, empty otherwise
     */
    @Transactional
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

    /**
     * Delete a note by ID.
     *
     * @param id the note ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deleteNote(Long id) {
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
