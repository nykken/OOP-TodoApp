package com.example.todo.dto;

import com.example.todo.util.EntityType;

import java.time.LocalDateTime;

/**
 * Common interface for items displayed on the dashboard.
 * Implemented by todo lists and notes.
 */
public interface DashboardItem {
    LocalDateTime getUpdatedAt();
    Long getId();
    String getProgressString();
    String getTitle();
    EntityType getEntityType();

    /**
     * Get completion percentage for progress display.
     * Only relevant for todo lists, defaults to 0.
     */
    default int getCompletionPercentage() {
        return 0;
    }

}
