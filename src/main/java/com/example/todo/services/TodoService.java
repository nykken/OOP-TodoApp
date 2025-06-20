package com.example.todo.services;

import com.example.todo.dto.TodoListRequest;
import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.entities.Todo;
import com.example.todo.entities.TodoList;
import com.example.todo.repositories.TodoListRepository;
import com.example.todo.repositories.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoListRepository todoListRepository;

    /**
     * Retrieve a todo list by ID.
     *
     * @param id the todo list ID
     * @return the todo list if found, empty otherwise
     */
    public Optional<TodoListResponse> getTodoList(Long id) {
        return todoListRepository.findById(id)
                .map(ConversionUtils::convertListToResponse);
    }

    /**
     * Retrieve all todo lists.
     *
     * @return list of all todo lists
     */
    public List<TodoListResponse> getAllTodoLists() {
        return todoListRepository.findAll().stream()
                .map(ConversionUtils::convertListToResponse)
                .toList();
    }

    /**
     * Create a new todo list.
     *
     * @param request the todo list data
     * @return the created todo list
     */
    @Transactional
    public TodoListResponse createTodoList(TodoListRequest request) {
        TodoList todoList = new TodoList(request.getName());
        TodoList savedTodoList = todoListRepository.save(todoList);
        return ConversionUtils.convertListToResponse(savedTodoList);
    }

    /**
     * Rename a todo list.
     *
     * @param id the todo list ID
     * @param request the updated todo list data
     * @return the updated todo list if found, empty otherwise
     */
    @Transactional
    public Optional<TodoListResponse> updateTodoList(Long id, TodoListRequest request) {
        return todoListRepository.findById(id)
                .map(todoList -> {
                    todoList.setName(request.getName());
                    TodoList updatedTodoList = todoListRepository.save(todoList);
                    return ConversionUtils.convertListToResponse(updatedTodoList);
                });
    }

    /**
     * Delete a todo list and all its todos.
     *
     * @param id the todo list ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deleteTodoList(Long id) {
        Optional<TodoList> todoList = todoListRepository.findById(id);
        if (todoList.isPresent()) {
            todoListRepository.delete(todoList.get());
            return true;
        }
        return false;
    }

    /**
     * Retrieve all todos in a list, sorted by completion status.
     *
     * @param listId the todo list ID
     * @return list of todos with incomplete items first
     */
    public List<TodoResponse> getTodos(Long listId) {
        List<Todo> todos = todoRepository.findByTodoListId(listId);
        return todos.stream()
                .map(ConversionUtils::convertTodoToResponse)
                .sorted((t1, t2) -> Boolean.compare(t1.isCompleted(), t2.isCompleted()))
                .toList();
    }

    /**
     * Retrieve todos in a list filtered by completion status.
     *
     * @param listId the todo list ID
     * @param completed the completion status filter
     * @return list of todos matching the completion status
     */
    public List<TodoResponse> getTodos(Long listId, Boolean completed) {
        List<Todo> todos = todoRepository.findByTodoListIdAndCompleted(listId, completed);
        return todos.stream()
                .map(ConversionUtils::convertTodoToResponse)
                .toList();
    }

    /**
     * Retrieve a specific todo by ID.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @return the todo if found, empty otherwise
     */
    public Optional<TodoResponse> getTodo(Long listId, Long todoId) {
        return todoRepository.findByIdAndTodoListId(todoId, listId)
                .map(ConversionUtils::convertTodoToResponse);
    }

    /**
     * Create a new todo in a list.
     *
     * @param listId the todo list ID
     * @param request the todo data
     * @return the created todo if parent list exists, empty otherwise
     */
    @Transactional
    public Optional<TodoResponse> createTodo(Long listId, TodoRequest request) {
        return todoListRepository.findById(listId)
                .map(todoList -> {
                    Todo todo = new Todo(request.getDescription(), todoList);
                    Todo savedTodo = todoRepository.save(todo);
                    return ConversionUtils.convertTodoToResponse(savedTodo);
                });
    }

    /**
     * Update a todo description.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @param request the updated todo data
     * @return the updated todo if found, empty otherwise
     */
    @Transactional
    public Optional<TodoResponse> updateTodo(Long listId, Long todoId, TodoRequest request) {
        return todoRepository.findByIdAndTodoListId(todoId, listId)
                .map(todo -> {
                    todo.setDescription(request.getDescription());
                    todo.getTodoList().setUpdatedAt(java.time.LocalDateTime.now());
                    Todo updatedTodo = todoRepository.save(todo);
                    return ConversionUtils.convertTodoToResponse(updatedTodo);
                });
    }

    /**
     * Delete a todo by ID.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deleteTodo(Long listId, Long todoId) {
        Optional<Todo> todo = todoRepository.findByIdAndTodoListId(todoId, listId);
        if (todo.isPresent()) {
            todoRepository.deleteById(todoId);
            return true;
        }
        return false;
    }

    /**
     * Mark a todo as complete.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @return the updated todo if found, empty otherwise
     */
    @Transactional
    public Optional<TodoResponse> markTodoAsComplete(Long listId, Long todoId) {
        return setTodoCompletionStatus(listId, todoId, true);
    }

    /**
     * Mark a todo as incomplete.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @return the updated todo if found, empty otherwise
     */
    @Transactional
    public Optional<TodoResponse> markTodoAsIncomplete(Long listId, Long todoId) {
        return setTodoCompletionStatus(listId, todoId, false);
    }

    /**
     * Update todo completion status and parent list timestamp.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @param completed the new completion status
     * @return the updated todo if found, empty otherwise
     */
    private Optional<TodoResponse> setTodoCompletionStatus(Long listId, Long todoId, boolean completed) {
        return todoRepository.findByIdAndTodoListId(todoId, listId)
                .map(todo -> {
                    todo.setCompleted(completed);
                    todo.getTodoList().setUpdatedAt(java.time.LocalDateTime.now());
                    Todo updatedTodo = todoRepository.save(todo);
                    return ConversionUtils.convertTodoToResponse(updatedTodo);
                });
    }
}
