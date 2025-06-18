package com.example.todo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteResponse {
    private Long id;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Add this helper method here
    public String getPreview() {
        if (body == null || body.trim().isEmpty()) {
            return "No content yet.";
        }
        return body.length() > 100 ? body.substring(0, 100) + "..." : body;
    }
}
