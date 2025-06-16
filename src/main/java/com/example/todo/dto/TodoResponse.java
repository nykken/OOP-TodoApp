package com.example.todo.dto;

import lombok.Data;

@Data
public class TodoResponse {
    private Long id;
    private String description;
    private boolean completed;
}
