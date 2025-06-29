package com.example.todo.services;

import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.NoteResponse;
import com.example.todo.entities.Todo;
import com.example.todo.entities.TodoList;
import com.example.todo.entities.Note;

import java.util.List;

/**
 * Utility class for converting entities to response DTOs.
 */
public class ConversionUtils {

    static TodoListResponse convertListToResponse(TodoList todoList) {
        TodoListResponse response = new TodoListResponse();
        response.setId(todoList.getId());
        response.setName(todoList.getName());
        response.setCreatedAt(todoList.getCreatedAt());
        response.setUpdatedAt(todoList.getUpdatedAt());

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

    static NoteResponse convertNoteToResponse(Note note) {
        NoteResponse response = new NoteResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setBody(note.getBody());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());

        return response;
    }
}
