package com.example.todo.service;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entities.Todo;
import com.example.todo.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    public List<TodoResponse> getAllTodos() {
        List<Todo> todos = todoRepository.findAll();
        return todos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Get todo by id
    public Optional<TodoResponse> getTodoById(Long id) {
        Optional<Todo> todo = todoRepository.findById(id);
        return todo.map(this::convertToResponse);
    }

    // Create new
    public TodoResponse createTodo(TodoRequest request) {
        Todo todo = convertToEntity(request);
        Todo savedTodo = todoRepository.save(todo);
        return convertToResponse(savedTodo);
    }

    public Optional<TodoResponse> completeTodo(Long id) {
        Optional<Todo> existingTodo = todoRepository.findById(id);

        if (existingTodo.isPresent()) {
            Todo todo = existingTodo.get();
            todo.setCompleted(true);

            Todo updatedTodo = todoRepository.save(todo);
            return Optional.of(convertToResponse(updatedTodo));
        }

        return Optional.empty();
    }

    // Update existing
    public Optional<TodoResponse> updateTodo(Long id, TodoRequest request) {
        Optional<Todo> existingTodo = todoRepository.findById(id);

        if (existingTodo.isPresent()) {
            Todo todo = existingTodo.get();
            todo.setDescription(request.getDescription());

            Todo updatedTodo = todoRepository.save(todo);
            return Optional.of(convertToResponse(updatedTodo));
        }

        return Optional.empty();
    }

    // Delete
    public boolean deleteTodo(Long id) {
        if (todoRepository.existsById(id)) {
            todoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Get todos by completion status
    public List<TodoResponse> getTodosByStatus(Boolean completed) {
        List<Todo> todos = todoRepository.findByCompleted(completed);
        return todos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<TodoResponse> searchTodos(String keyword) {
        List<Todo> todos = todoRepository.findByDescriptionContainingIgnoreCase(keyword);
        return todos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Convert Entity to Response DTO
    private TodoResponse convertToResponse(Todo todo) {
        TodoResponse response = new TodoResponse();
        response.setId(todo.getId());
        response.setDescription(todo.getDescription());
        response.setCompleted(todo.getCompleted());
        response.setCreatedAt(todo.getCreatedAt());
        response.setUpdatedAt(todo.getUpdatedAt());
        return response;
    }

    // Convert Request DTO to Entity
    private Todo convertToEntity(TodoRequest request) {
        Todo todo = new Todo();
        todo.setDescription(request.getDescription());
        todo.setCompleted(false);
        return todo;
    }
}
