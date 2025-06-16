package com.example.todo.dto;

import lombok.Data;

@Data
public class NoteRequest {
    private String title;
    private String body;
}
