package com.example.todo.services;

import com.example.todo.dto.TodoListRequest;
import com.example.todo.dto.TodoListResponse;
import com.example.todo.entities.TodoList;
import com.example.todo.repositories.TodoListRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TodoListService {

    @Autowired
    private TodoListRepository todoListRepository;

    public Optional<TodoListResponse> getTodoList(Long id) {
        return todoListRepository.findById(id)
                .map(ConversionUtils::convertListToResponse);
    }

    public List<TodoListResponse> getAllTodoLists() {
        return todoListRepository.findAll().stream()
                .map(ConversionUtils::convertListToResponse)
                .toList();
    }

    public TodoListResponse createTodoList(TodoListRequest request) {
        TodoList todoList = new TodoList(request.getName());
        TodoList savedTodoList = todoListRepository.save(todoList);
        return ConversionUtils.convertListToResponse(savedTodoList);
    }

    public Optional<TodoListResponse> updateTodoList(Long id, TodoListRequest request) {
        return todoListRepository.findById(id)
                .map(todoList -> {
                    todoList.setName(request.getName());
                    TodoList updatedTodoList = todoListRepository.save(todoList);
                    return ConversionUtils.convertListToResponse(updatedTodoList);
                });
    }

    @Transactional
    public boolean deleteTodoList(Long id) {
        if (todoListRepository.existsById(id)) {
            todoListRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Check if TodoList exists
    public boolean exists(Long id) {
        return todoListRepository.existsById(id);
    }

    public Optional<TodoList> getTodoListEntity(Long id) {
        return todoListRepository.findById(id);
    }
}
