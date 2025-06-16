package com.example.todo.controller;

import com.example.todo.dto.TodoListRequest;
import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.services.TodoListService;
import com.example.todo.services.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class TodoWebController {

    @Autowired
    private TodoListService todoListService;

    @Autowired
    private TodoService todoService;


    // Home page - show all todo lists
    @GetMapping
    public String home(Model model) {
        List<TodoListResponse> todoLists = todoListService.getAllTodoLists();
        model.addAttribute("todoLists", todoLists);
        model.addAttribute("newTodoList", new TodoListRequest());
        return "index";
    }

    @GetMapping("/test")
    public String testThymeleaf(Model model) {
        List<TodoListResponse> todoLists = todoListService.getAllTodoLists();
        model.addAttribute("todoLists", todoLists);
        model.addAttribute("newTodoList", new TodoListRequest());
        return "test";
    }

    // Create new todo list
    @PostMapping("/lists")
    public String createTodoList(@Valid @ModelAttribute("newTodoList") TodoListRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            List<TodoListResponse> todoLists = todoListService.getAllTodoLists();
            model.addAttribute("todoLists", todoLists);
            return "index";
        }

        TodoListResponse createdList = todoListService.createTodoList(request);
        redirectAttributes.addFlashAttribute("successMessage", "Todo list created successfully!");
        return "redirect:/lists/" + createdList.getId();
    }

    // Show specific todo list with todos
    @GetMapping("/lists/{listId}")
    public String viewTodoList(@PathVariable Long listId,
                               @RequestParam(value = "filter", required = false) String filter,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> todoListOpt = todoListService.getTodoList(listId);

        if (todoListOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
            return "redirect:/";
        }

        TodoListResponse todoList = todoListOpt.get();
        List<TodoResponse> todos;

        // Apply filter if specified
        if ("completed".equals(filter)) {
            todos = todoService.getTodos(listId, true);
        } else if ("pending".equals(filter)) {
            todos = todoService.getTodos(listId, false);
        } else {
            todos = todoService.getTodos(listId);
        }

        model.addAttribute("todoList", todoList);
        model.addAttribute("todos", todos);
        model.addAttribute("newTodo", new TodoRequest());
        model.addAttribute("currentFilter", filter);
        model.addAttribute("editTodoListRequest", new TodoListRequest());

        return "todo-list";
    }

    // Create new todo in a list
    @PostMapping("/lists/{listId}/todos")
    public String createTodo(@PathVariable Long listId,
                             @Valid @ModelAttribute("newTodo") TodoRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            Optional<TodoListResponse> todoListOpt = todoListService.getTodoList(listId);
            if (todoListOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
                return "redirect:/";
            }

            TodoListResponse todoList = todoListOpt.get();
            List<TodoResponse> todos = todoService.getTodos(listId);

            model.addAttribute("todoList", todoList);
            model.addAttribute("todos", todos);
            model.addAttribute("editTodoListRequest", new TodoListRequest());
            return "todo-list";
        }

        Optional<TodoResponse> createdTodo = todoService.createTodo(listId, request);
        if (createdTodo.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create todo!");
        }

        return "redirect:/lists/" + listId;
    }

    // Toggle todo completion status
    @PostMapping("/lists/{listId}/todos/{todoId}/toggle")
    public String toggleTodoCompletion(@PathVariable Long listId,
                                       @PathVariable Long todoId,
                                       RedirectAttributes redirectAttributes) {
        Optional<TodoResponse> todoOpt = todoService.getTodo(listId, todoId);

        if (todoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo not found!");
            return "redirect:/lists/" + listId;
        }

        TodoResponse todo = todoOpt.get();
        Optional<TodoResponse> updatedTodo;

        if (todo.getCompleted()) {
            updatedTodo = todoService.markTodoAsIncomplete(listId, todoId);
        } else {
            updatedTodo = todoService.markTodoAsComplete(listId, todoId);
        }

        if (updatedTodo.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo!");
        }

        return "redirect:/lists/" + listId;
    }

    // Delete todo
    @PostMapping("/lists/{listId}/todos/{todoId}/delete")
    public String deleteTodo(@PathVariable Long listId,
                             @PathVariable Long todoId,
                             RedirectAttributes redirectAttributes) {
        boolean deleted = todoService.deleteTodo(listId, todoId);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete todo!");
        }

        return "redirect:/lists/" + listId;
    }

    // Update todo list name
    @PostMapping("/lists/{listId}/update")
    public String updateTodoList(@PathVariable Long listId,
                                 @Valid @ModelAttribute("editTodoListRequest") TodoListRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            Optional<TodoListResponse> todoListOpt = todoListService.getTodoList(listId);
            if (todoListOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
                return "redirect:/";
            }

            TodoListResponse todoList = todoListOpt.get();
            List<TodoResponse> todos = todoService.getTodos(listId);

            model.addAttribute("todoList", todoList);
            model.addAttribute("todos", todos);
            model.addAttribute("newTodo", new TodoRequest());
            return "todo-list";
        }

        Optional<TodoListResponse> updatedList = todoListService.updateTodoList(listId, request);

        if (updatedList.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo list updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo list!");
        }

        return "redirect:/lists/" + listId;
    }

    // Delete todo list
    @PostMapping("/lists/{listId}/delete")
    public String deleteTodoList(@PathVariable Long listId,
                                 RedirectAttributes redirectAttributes) {
        boolean deleted = todoListService.deleteTodoList(listId);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo list deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete todo list!");
        }

        return "redirect:/";
    }

    // Edit todo form
    @GetMapping("/lists/{listId}/todos/{todoId}/edit")
    public String editTodoForm(@PathVariable Long listId,
                               @PathVariable Long todoId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<TodoResponse> todoOpt = todoService.getTodo(listId, todoId);
        Optional<TodoListResponse> todoListOpt = todoListService.getTodoList(listId);

        if (todoOpt.isEmpty() || todoListOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo or list not found!");
            return "redirect:/lists/" + listId;
        }

        TodoResponse todo = todoOpt.get();
        TodoRequest editRequest = new TodoRequest();
        editRequest.setDescription(todo.getDescription());

        model.addAttribute("todoList", todoListOpt.get());
        model.addAttribute("todo", todo);
        model.addAttribute("editTodoRequest", editRequest);

        return "edit-todo";
    }

    // Update todo
    @PostMapping("/lists/{listId}/todos/{todoId}/update")
    public String updateTodo(@PathVariable Long listId,
                             @PathVariable Long todoId,
                             @Valid @ModelAttribute("editTodoRequest") TodoRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            Optional<TodoResponse> todoOpt = todoService.getTodo(listId, todoId);
            Optional<TodoListResponse> todoListOpt = todoListService.getTodoList(listId);

            if (todoOpt.isEmpty() || todoListOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Todo or list not found!");
                return "redirect:/lists/" + listId;
            }

            model.addAttribute("todoList", todoListOpt.get());
            model.addAttribute("todo", todoOpt.get());
            return "edit-todo";
        }

        Optional<TodoResponse> updatedTodo = todoService.updateTodo(listId, todoId, request);

        if (updatedTodo.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo!");
        }

        return "redirect:/lists/" + listId;
    }
}