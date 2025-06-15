package com.example.todo.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class TodoRequest {
    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Todo list ID is required")
    private Long todoListId;
}