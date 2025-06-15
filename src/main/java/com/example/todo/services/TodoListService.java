package com.example.todo.services;

import com.example.todo.dto.TodoListRequest;
import com.example.todo.dto.TodoListResponse;
import com.example.todo.entities.TodoList;
import com.example.todo.repositories.TodoListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodoListService {

    @Autowired
    private TodoListRepository todoListRepository;

    public Optional<TodoListResponse> getTodoListById(Long id) {
        return todoListRepository.findById(id)
                .map(ConversionUtils::convertListToResponse);
    }

    public List<TodoListResponse> getAllTodoLists() {
        return todoListRepository.findAll().stream()
                .map(ConversionUtils::convertListToResponse)
                .collect(Collectors.toList());
    }

    public TodoListResponse createTodoList(TodoListRequest request) {
        TodoList todoList = new TodoList(request.getName());
        TodoList savedTodoList = todoListRepository.save(todoList);
        return ConversionUtils.convertListToResponse(savedTodoList);
    }

    public Optional<TodoListResponse> updateTodoList(Long id, TodoListRequest request) {
        Optional<TodoList> existingTodoList = todoListRepository.findById(id);

        if (existingTodoList.isPresent()) {
            TodoList todoList = existingTodoList.get();
            todoList.setName(request.getName());

            TodoList updatedTodoList = todoListRepository.save(todoList);
            return Optional.of(ConversionUtils.convertListToResponse(updatedTodoList));
        }

        return Optional.empty();
    }

    public boolean deleteTodoList(Long id) {
        if (todoListRepository.existsById(id)) {
            todoListRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Internal method for TodoService (still returns entity)
    public TodoList getTodoListEntityById(Long id) {
        return todoListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TodoList not found with id: " + id));
    }

}
