package com.example.todo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TodoListResponse implements DashboardItem {
    private Long id;
    private String name;
    private List<TodoResponse> todos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public int getTotalTodos() {
        return todos != null ? todos.size() : 0;
    }

    public int getCompletedTodos() {
        return todos != null
                ? (int) todos.stream().filter(TodoResponse::isCompleted).count()
                : 0;
    }

    public int getPendingTodos() {
        return getTotalTodos() - getCompletedTodos();
    }

    public String getProgressString() {
        return getCompletedTodos() + "/" + getTotalTodos();
    }

    public String getTitle() {
        return name;
    }

    public String getPreview() {
        return "this is a todolist preview tralala";
    }
}
