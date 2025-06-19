package com.example.todo.dto;

import com.example.todo.util.EntityType;

import java.time.LocalDateTime;

public interface DashboardItem {
    LocalDateTime getUpdatedAt();
    Long getId();
    String getPreview();
    String getProgressString();
    String getTitle();
    EntityType getEntityType();
    String getTimestampString();

    // Only relevant for Todo lists
    default int getCompletionPercentage() {
        return 0;
    }

}
