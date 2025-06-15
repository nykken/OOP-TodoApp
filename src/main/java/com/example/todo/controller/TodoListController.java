package com.example.todo.controller;

import com.example.todo.dto.TodoListRequest;
import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.services.TodoService;
import com.example.todo.services.TodoListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/todo-lists")
@CrossOrigin(origins = "*")
public class TodoListController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoListService todoListService;

    // ---- LIST OPERATIONS ----

    @GetMapping
    public ResponseEntity<List<TodoListResponse>> getAllTodoLists() {
        List<TodoListResponse> todoLists = todoListService.getAllTodoLists();
        return ResponseEntity.ok(todoLists);
    }

    @GetMapping("/{listId}")
    public ResponseEntity<TodoListResponse> getTodoListById(@PathVariable Long listId) {
        Optional<TodoListResponse> todoList = todoListService.getTodoList(listId);
        return todoList.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TodoListResponse> createTodoList(@Valid @RequestBody TodoListRequest request) {
        TodoListResponse createdTodoList = todoListService.createTodoList(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodoList);
    }

    // Rename list
    @PutMapping("/{listId}")
    public ResponseEntity<TodoListResponse> updateTodoList(
            @PathVariable Long listId,
            @Valid @RequestBody TodoListRequest request) {
        Optional<TodoListResponse> updatedTodoList = todoListService.updateTodoList(listId, request);
        return updatedTodoList.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteTodoList(@PathVariable Long listId) {
        boolean deleted = todoListService.deleteTodoList(listId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ---- TODO OPERATIONS ----

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

    @GetMapping("/{listId}/todos/{todoId}")
    public ResponseEntity<TodoResponse> getTodo(
            @PathVariable Long listId,
            @PathVariable Long todoId) {

        Optional<TodoResponse> todo = todoService.getTodo(listId, todoId);
        return todo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{listId}/todos")
    public ResponseEntity<TodoResponse> createTodo(
            @PathVariable Long listId,
            @Valid @RequestBody TodoRequest request) {

        Optional<TodoResponse> createdTodo = todoService.createTodo(listId, request);
        return createdTodo.map(todo -> ResponseEntity.status(HttpStatus.CREATED).body(todo))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{listId}/todos/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long listId,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoRequest request) {

        Optional<TodoResponse> updatedTodo = todoService.updateTodo(listId, todoId, request);
        return updatedTodo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{listId}/todos/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable Long listId,
            @PathVariable Long todoId) {

        boolean deleted = todoService.deleteTodo(listId, todoId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{listId}/todos/{todoId}/complete")
    public ResponseEntity<TodoResponse> markTodoAsComplete(
            @PathVariable Long listId,
            @PathVariable Long todoId) {

        Optional<TodoResponse> updatedTodo = todoService.markTodoAsComplete(listId, todoId);
        return updatedTodo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{listId}/todos/{todoId}/incomplete")
    public ResponseEntity<TodoResponse> markTodoAsIncomplete(
            @PathVariable Long listId,
            @PathVariable Long todoId) {

        Optional<TodoResponse> updatedTodo = todoService.markTodoAsIncomplete(listId, todoId);
        return updatedTodo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }
}
