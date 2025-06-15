package com.example.todo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TodoRequest {
    @NotBlank(message = "Description is required")
    private String description;
}