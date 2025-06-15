package com.example.todo.services;

import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entities.Todo;
import com.example.todo.entities.TodoList;

import java.util.List;


public class ConversionUtils {

    static TodoListResponse convertListToResponse(TodoList todoList) {
        TodoListResponse response = new TodoListResponse();
        response.setId(todoList.getId());
        response.setName(todoList.getName());

        List<TodoResponse> entries = todoList.getTodos()
                .stream()
                .map(ConversionUtils::convertEntryToResponse)
                .toList();

        response.setEntries(entries);

        return response;
    }

    static TodoResponse convertEntryToResponse(Todo todo) {
        TodoResponse response = new TodoResponse();
        response.setId(todo.getId());
        response.setDescription(todo.getDescription());
        response.setCompleted(todo.getCompleted());

        return response;
    }
}
