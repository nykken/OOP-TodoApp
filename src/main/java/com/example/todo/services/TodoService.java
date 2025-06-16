package com.example.todo.services;

import com.example.todo.dto.TodoListRequest;
import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entities.Todo;
import com.example.todo.entities.TodoList;
import com.example.todo.repositories.TodoListRepository;
import com.example.todo.repositories.TodoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

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

    public int getTotalTodoCount(Long listId) {
        return getTodos(listId).size();
    }

    public int getCompletedTodoCount(Long listId) {
        return (int) getTodos(listId).stream()
                .filter(TodoResponse::isCompleted)
                .count();
    }

    public Optional<TodoList> getTodoListEntity(Long id) {
        return todoListRepository.findById(id);
    }

    // Get all todos in a list
    public List<TodoResponse> getTodos(Long listId) {
        if (!exists(listId)) {
            return Collections.emptyList();
        }

        // Sort the todos - pending first
        List<Todo> todos = todoRepository.findByTodoListId(listId);
        return todos.stream()
                .map(ConversionUtils::convertTodoToResponse)
                .sorted((t1, t2) -> Boolean.compare(t1.isCompleted(), t2.isCompleted()))
                .toList();
    }

    // Get todos in a list, filtered by completion status
    public List<TodoResponse> getTodos(Long listId, Boolean completed) {
        if (!exists(listId)) {
            return Collections.emptyList();
        }

        List<Todo> todos = todoRepository.findByTodoListIdAndCompleted(listId, completed);
        return todos.stream()
                .map(ConversionUtils::convertTodoToResponse)
                .toList();
    }

    public Optional<TodoResponse> getTodo(Long listId, Long todoId) {
        if (!exists(listId)) {
            return Optional.empty();
        }

        return todoRepository.findByIdAndTodoListId(todoId, listId)
                .map(ConversionUtils::convertTodoToResponse);
    }

    public Optional<TodoResponse> createTodo(Long listId, TodoRequest request) {
        return getTodoListEntity(listId)
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
