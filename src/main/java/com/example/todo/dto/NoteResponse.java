package com.example.todo.dto;

import com.example.todo.util.EntityType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteResponse implements DashboardItem {
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

    // Notes do not have progress
    public String getProgressString() {
        return null;
    }

    public EntityType getEntityType() {
        return EntityType.NOTE;
    }
}
