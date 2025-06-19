package com.example.todo.dto;

import com.example.todo.util.EntityType;
import lombok.Data;

@Data
public class TodoResponse {
    private Long id;
    private String description;
    private boolean completed;

}
