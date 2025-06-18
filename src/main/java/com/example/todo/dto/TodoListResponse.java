package com.example.todo.dto;

import lombok.Data;

import java.util.List;

@Data
public class TodoListResponse {
    private Long id;
    private String name;
    private List<TodoResponse> todos;

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
}
