package com.example.todo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TodoResponse {
    private Long id;
    private String description;
    private Boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
