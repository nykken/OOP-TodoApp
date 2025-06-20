package com.example.todo.dto;

import com.example.todo.util.EntityType;
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

    public String getProgressString() {
        return getCompletedTodos() + "/" + getTotalTodos();
    }

    @Override
    public int getCompletionPercentage() {
        if (getTotalTodos() == 0) {
            return 0;
        }
        return (int) Math.round((double) getCompletedTodos() / getTotalTodos() * 100.0);
    }

    public String getTitle() {
        return name;
    }

    public EntityType getEntityType() {
        return EntityType.TODOLIST;
    }
}
