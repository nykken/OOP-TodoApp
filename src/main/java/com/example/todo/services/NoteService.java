package com.example.todo.services;

import com.example.todo.dto.NoteRequest;
import com.example.todo.dto.NoteResponse;
import com.example.todo.entities.Note;
import com.example.todo.repositories.NoteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    @Autowired
    private NoteRepository todoListRepository;




}
