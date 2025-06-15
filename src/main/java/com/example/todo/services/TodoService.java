package com.example.todo.services;

import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entities.Todo;
import com.example.todo.repositories.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoListService todoListService;

    // Get all todos in a list
    public List<TodoResponse> getTodos(Long listId) {
        if (!todoListService.exists(listId)) {
            return Collections.emptyList(); // or throw exception if you prefer
        }

        // Sort the todos - pending first
        List<Todo> todos = todoRepository.findByTodoListId(listId);
        return todos.stream()
                .map(ConversionUtils::convertTodoToResponse)
                .sorted((t1, t2) -> Boolean.compare(t1.getCompleted(), t2.getCompleted()))
                .collect(Collectors.toList());
    }

    // Get todos in a list, filtered by completion status
    public List<TodoResponse> getTodos(Long listId, Boolean completed) {
        if (!todoListService.exists(listId)) {
            return Collections.emptyList();
        }

        List<Todo> todos = todoRepository.findByTodoListIdAndCompleted(listId, completed);
        return todos.stream()
                .map(ConversionUtils::convertTodoToResponse)
                .collect(Collectors.toList());
    }

    public Optional<TodoResponse> getTodo(Long listId, Long todoId) {
        if (!todoListService.exists(listId)) {
            return Optional.empty();
        }

        return todoRepository.findByIdAndTodoListId(todoId, listId)
                .map(ConversionUtils::convertTodoToResponse);
    }

    public Optional<TodoResponse> createTodo(Long listId, TodoRequest request) {
        return todoListService.getTodoListEntity(listId)
                .map(todoList -> {
                    Todo todo = new Todo(request.getDescription(), todoList);
                    Todo savedTodo = todoRepository.save(todo);
                    return ConversionUtils.convertTodoToResponse(savedTodo);
                });

    }

    public Optional<TodoResponse> updateTodo(Long listId, Long todoId, TodoRequest request) {
        return todoRepository.findByIdAndTodoListId(todoId, listId)
                .map(todo -> {
                    todo.setDescription(request.getDescription());
                    Todo updatedTodo = todoRepository.save(todo);
                    return ConversionUtils.convertTodoToResponse(updatedTodo);
                });
    }

    public boolean deleteTodo(Long listId, Long todoId) {
        Optional<Todo> todo = todoRepository.findByIdAndTodoListId(todoId, listId);
        if (todo.isPresent()) {
            todoRepository.deleteById(todoId);
            return true;
        }
        return false;
    }

    public Optional<TodoResponse> markTodoAsComplete(Long listId, Long todoId) {
        return setTodoCompletionStatus(listId, todoId, true);
    }

    public Optional<TodoResponse> markTodoAsIncomplete(Long listId, Long todoId) {
        return setTodoCompletionStatus(listId, todoId, false);
    }

    // Helper method to change completion status
    private Optional<TodoResponse> setTodoCompletionStatus(Long listId, Long todoId, boolean completed) {
        return todoRepository.findByIdAndTodoListId(todoId, listId)
                .map(todo -> {
                    todo.setCompleted(completed);
                    Todo updatedTodo = todoRepository.save(todo);
                    return ConversionUtils.convertTodoToResponse(updatedTodo);
                });
    }
}
