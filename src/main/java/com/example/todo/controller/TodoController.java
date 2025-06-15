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
@RequestMapping("/lists")
@CrossOrigin(origins = "*")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoListService todoListService;

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos(
            @RequestParam(required = false) Boolean completed) {
        List<TodoResponse> todos;

        if (completed != null) {
            // Filter by completion status
            todos = todoService.getTodosByStatus(completed);
        } else {
            // Get all todos
            todos = todoService.getAllTodos();
        }

        return ResponseEntity.ok(todos);
    }

    @GetMapping
    public ResponseEntity<List<TodoListResponse>> getAllTodoLists() {
        List<TodoListResponse> todoLists = todoListService.getAllTodoLists();

        return ResponseEntity.ok(todoLists);
    }




    // GET /lists/1 - Get todo list by id
    @GetMapping("/{id}")
    public ResponseEntity<TodoListResponse> getTodoById(@PathVariable Long id) {
        Optional<TodoListResponse> todoList = todoListService.getTodoListById(id);

        return todoList.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /todos - Create new todo
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody TodoRequest request) {
        TodoResponse createdTodo = todoService.createTodo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }

    // POST /todos/1/complete
    @PostMapping("/{id}/complete")
    public ResponseEntity<TodoResponse> completeTodo(@PathVariable Long id) {
        Optional<TodoResponse> todo = todoService.completeTodo(id);

        return todo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /todos/1 - Update todo by ID
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request) {

        Optional<TodoResponse> updatedTodo = todoService.updateTodo(id, request);

        return updatedTodo.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /todos/1 - Delete todo by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        boolean deleted = todoService.deleteTodo(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /todos/search?q=keyword - Search todos
    @GetMapping("/search")
    public ResponseEntity<List<TodoResponse>> searchTodos(@RequestParam String q) {
        List<TodoResponse> todos = todoService.searchTodos(q);
        return ResponseEntity.ok(todos);
    }
}
