package com.example.todo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TodoListRequest {
    @NotBlank(message = "Name is required")
    private String name;
}