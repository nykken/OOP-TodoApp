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

        List<TodoResponse> todos = todoList.getTodos()
                .stream()
                .map(ConversionUtils::convertTodoToResponse)
                .toList();

        response.setTodos(todos);

        return response;
    }

    static TodoResponse convertTodoToResponse(Todo todo) {
        TodoResponse response = new TodoResponse();
        response.setId(todo.getId());
        response.setDescription(todo.getDescription());
        response.setCompleted(todo.getCompleted());

        return response;
    }
}
