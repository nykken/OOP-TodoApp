package com.example.todo.dto;

import lombok.Data;

import java.util.List;

@Data
public class TodoListResponse {
    private Long id;
    private String name;
    private List<TodoResponse> entries;
}
