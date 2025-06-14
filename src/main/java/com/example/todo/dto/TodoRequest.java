package com.example.todo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;


@Data
public class TodoRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}