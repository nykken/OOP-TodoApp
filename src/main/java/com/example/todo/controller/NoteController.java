package com.example.todo.controller;

import com.example.todo.dto.NoteRequest;
import com.example.todo.dto.NoteResponse;
import com.example.todo.services.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;


/**
 * REST controller for managing notes.
 * Provides CRUD operations for notes at /api/notes.
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    /**
     * Retrieve all notes.
     *
     * @return list of all notes
     */
    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes() {
        List<NoteResponse> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    /**
     * Retrieve a note by ID.
     *
     * @param id the note ID
     * @return the note if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        Optional<NoteResponse> note = noteService.getNote(id);
        return note.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update an existing note.
     *
     * @param id the note ID
     * @param request the updated note data
     * @return the updated note if found, 404 otherwise
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        Optional<NoteResponse> updatedNote = noteService.updateNote(id, request);
        return updatedNote.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /**
     * Delete a note by ID.
     *
     * @param id the note ID
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        if (noteService.deleteNote(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new note.
     *
     * @param request the note data
     * @return the created note with generated ID
     */
    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        NoteResponse createdNote = noteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }
}
