package com.example.todo.dto;

import java.time.LocalDateTime;

public interface DashboardItem {
    LocalDateTime getUpdatedAt();
    public Long getId();
    public String getPreview();
    public String getProgressString();
    public String getTitle();
}
