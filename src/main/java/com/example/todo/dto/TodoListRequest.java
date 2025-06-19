package com.example.todo.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TodoListRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;
}