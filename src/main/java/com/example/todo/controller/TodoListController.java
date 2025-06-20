package com.example.todo.controller;

import com.example.todo.dto.TodoListRequest;
import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.services.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing todo lists and their associated todo items.
 * Provides CRUD operations for todo lists and todos at /api/todo-lists.
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/todo-lists")
public class TodoListController {

    private final TodoService todoService;

    // ===================== LIST OPERATIONS =====================

    /**
     * Retrieve all todo lists.
     *
     * @return list of all todo lists
     */
    @GetMapping
    public ResponseEntity<List<TodoListResponse>> getAllTodoLists() {
        List<TodoListResponse> todoLists = todoService.getAllTodoLists();
        return ResponseEntity.ok(todoLists);
    }

    /**
     * Retrieve a todo list by ID.
     *
     * @param listId the todo list ID
     * @return the todo list if found, 404 otherwise
     */
    @GetMapping("/{listId}")
    public ResponseEntity<TodoListResponse> getTodoListById(@PathVariable Long listId) {
        Optional<TodoListResponse> todoList = todoService.getTodoList(listId);
        return todoList.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new todo list.
     *
     * @param request the todo list data
     * @return the created todo list with generated ID
     */
    @PostMapping
    public ResponseEntity<TodoListResponse> createTodoList(@Valid @RequestBody TodoListRequest request) {
        TodoListResponse createdTodoList = todoService.createTodoList(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodoList);
    }

    /**
     * Update an existing todo list.
     *
     * @param listId the todo list ID
     * @param request the updated todo list data
     * @return the updated todo list if found, 404 otherwise
     */
    @PutMapping("/{listId}")
    public ResponseEntity<TodoListResponse> updateTodoList(
            @PathVariable Long listId,
            @Valid @RequestBody TodoListRequest request) {
        Optional<TodoListResponse> updatedTodoList = todoService.updateTodoList(listId, request);
        return updatedTodoList.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    /**
     * Delete a todo list by ID.
     *
     * @param listId the todo list ID
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteTodoList(@PathVariable Long listId) {
        if (todoService.deleteTodoList(listId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ===================== TODO OPERATIONS =====================

    /**
     * Retrieve todos from a todo list with optional filtering.
     *
     * @param listId the todo list ID
     * @param completed filter by completion status (true/false/null for all)
     * @return list of todos
     */
    @GetMapping("/{listId}/todos")
    public ResponseEntity<List<TodoResponse>> getTodos(
            @PathVariable Long listId,
            @RequestParam(required = false) Boolean completed) {

        List<TodoResponse> todos;
        if (completed != null) {
            todos = todoService.getTodos(listId, completed);
        } else {
            todos = todoService.getTodos(listId);
        }
        return ResponseEntity.ok(todos);
    }

    /**
     * Retrieve a todo by ID.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @return the todo if found, 404 otherwise
     */
    @GetMapping("/{listId}/todos/{todoId}")
    public ResponseEntity<TodoResponse> getTodo(
            @PathVariable Long listId,
            @PathVariable Long todoId) {

        Optional<TodoResponse> todo = todoService.getTodo(listId, todoId);
        return todo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new todo in a todo list.
     *
     * @param listId the todo list ID
     * @param request the todo data
     * @return the created todo if parent list exists, 404 otherwise
     */
    @PostMapping("/{listId}/todos")
    public ResponseEntity<TodoResponse> createTodo(
            @PathVariable Long listId,
            @Valid @RequestBody TodoRequest request) {

        Optional<TodoResponse> createdTodo = todoService.createTodo(listId, request);
        return createdTodo.map(todo -> ResponseEntity.status(HttpStatus.CREATED).body(todo))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Update an existing todo.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @param request the updated todo data
     * @return the updated todo if found, 404 otherwise
     */
    @PutMapping("/{listId}/todos/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long listId,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoRequest request) {

        Optional<TodoResponse> updatedTodo = todoService.updateTodo(listId, todoId, request);
        return updatedTodo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a todo by ID.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @return 204 if deleted, 404 if not found
     */
    @DeleteMapping("/{listId}/todos/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable Long listId,
            @PathVariable Long todoId) {

        if (todoService.deleteTodo(listId, todoId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Mark a todo as complete.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @return the updated todo if found, 404 otherwise
     */
    @PatchMapping("/{listId}/todos/{todoId}/complete")
    public ResponseEntity<TodoResponse> markTodoAsComplete(
            @PathVariable Long listId,
            @PathVariable Long todoId) {

        Optional<TodoResponse> updatedTodo = todoService.markTodoAsComplete(listId, todoId);
        return updatedTodo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Mark a todo as incomplete.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @return the updated todo if found, 404 otherwise
     */
    @PatchMapping("/{listId}/todos/{todoId}/incomplete")
    public ResponseEntity<TodoResponse> markTodoAsIncomplete(
            @PathVariable Long listId,
            @PathVariable Long todoId) {

        Optional<TodoResponse> updatedTodo = todoService.markTodoAsIncomplete(listId, todoId);
        return updatedTodo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }
}
