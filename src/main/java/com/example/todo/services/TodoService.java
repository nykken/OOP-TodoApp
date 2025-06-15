package com.example.todo.services;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entities.Todo;
import com.example.todo.entities.TodoList;
import com.example.todo.repositories.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoListService todoListService;

    public List<TodoResponse> getAllTodos() {
        List<Todo> todos = todoRepository.findAll();
        return todos.stream()
                .map(ConversionUtils::convertEntryToResponse)
                .collect(Collectors.toList());
    }

    public Optional<TodoResponse> getTodoById(Long id) {
        return todoRepository.findById(id)
                .map(ConversionUtils::convertEntryToResponse);
    }

    // Create new
    public TodoResponse createTodo(TodoRequest request) {
        Todo todo = convertToEntity(request);
        Todo savedTodo = todoRepository.save(todo);
        return ConversionUtils.convertEntryToResponse(savedTodo);
    }

    public Optional<TodoResponse> completeTodo(Long id) {
        Optional<Todo> existingTodo = todoRepository.findById(id);

        if (existingTodo.isPresent()) {
            Todo todo = existingTodo.get();
            todo.setCompleted(true);

            Todo updatedTodo = todoRepository.save(todo);
            return Optional.of(ConversionUtils.convertEntryToResponse(updatedTodo));
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
            return Optional.of(ConversionUtils.convertEntryToResponse(updatedTodo));
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
                .map(ConversionUtils::convertEntryToResponse)
                .collect(Collectors.toList());
    }

    public List<TodoResponse> searchTodos(String keyword) {
        List<Todo> todos = todoRepository.findByDescriptionContainingIgnoreCase(keyword);
        return todos.stream()
                .map(ConversionUtils::convertEntryToResponse)
                .collect(Collectors.toList());
    }

    private Todo convertToEntity(TodoRequest request) {
        TodoList todoList = todoListService.getTodoListEntityById(request.getTodoListId());

        Todo todo = new Todo();
        todo.setDescription(request.getDescription());
        todo.setCompleted(false);
        todo.setTodoList(todoList);
        return todo;
    }

}
