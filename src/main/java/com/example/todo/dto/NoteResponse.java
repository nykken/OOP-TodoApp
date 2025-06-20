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

    // Notes do not have progress
    public String getProgressString() {
        return null;
    }

    public EntityType getEntityType() {
        return EntityType.NOTE;
    }

}
