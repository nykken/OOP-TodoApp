package com.example.todo.controller;

import com.example.todo.dto.TodoListRequest;
import com.example.todo.dto.TodoListResponse;
import com.example.todo.dto.TodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.services.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class WebController {

    private final TodoService todoService;

    // Home page - show all todo lists
    @GetMapping
    public String home(Model model) {
        List<TodoListResponse> todoLists = todoService.getAllTodoLists();
        model.addAttribute("todoLists", todoLists);
        model.addAttribute("newTodoList", new TodoListRequest());
        return "lists";
    }

    // TODO DELETE THIS
    @GetMapping("/test")
    public String testThymeleaf(Model model) {
        List<TodoListResponse> todoLists = todoService.getAllTodoLists();
        model.addAttribute("todoLists", todoLists);
        model.addAttribute("newTodoList", new TodoListRequest());
        return "test";
    }



    // ===================== LIST OPERATIONS ==================
    @GetMapping("/lists/new")
    public String newTodoList(Model model) {
        model.addAttribute("newTodoList", new TodoListRequest());
        return "lists/new";
    }

    @PostMapping("/lists")
    public String createTodoList(@Valid @ModelAttribute("newTodoList") TodoListRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            List<TodoListResponse> todoLists = todoService.getAllTodoLists();
            model.addAttribute("todoLists", todoLists);
            return "lists";
        }

        TodoListResponse createdList = todoService.createTodoList(request);
        return "redirect:/lists/" + createdList.getId();
    }

    // Show specific todo list with todos
    @GetMapping("/lists/{listId}")
    public String viewTodoList(@PathVariable Long listId,
                               @RequestParam(value = "filter", required = false) String filter,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        return todoService.getTodoList(listId)
                .map(todoList -> {
                    List<TodoResponse> allTodos = todoService.getTodos(listId);
                    List<TodoResponse> filteredTodos = Optional.ofNullable(filter)
                            .map(f -> switch (f) {
                                case "completed" -> allTodos.stream()
                                        .filter(TodoResponse::isCompleted)
                                        .collect(Collectors.toList());
                                case "pending" -> allTodos.stream()
                                        .filter(todo -> !todo.isCompleted())
                                        .collect(Collectors.toList());
                                default -> allTodos;
                            })
                            .orElse(allTodos);

                    // Populate model
                    model.addAttribute("todoList", todoList);
                    model.addAttribute("todos", filteredTodos);
                    model.addAttribute("newTodo", new TodoRequest());
                    model.addAttribute("currentFilter", filter);
                    model.addAttribute("editTodoListRequest", new TodoListRequest());
                    model.addAttribute("hasTodos", !allTodos.isEmpty());

                    return "lists/details";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
                    return "redirect:/";
                });
    }

    // Create new todo in a list
    @PostMapping("/lists/{listId}/todos")
    public String createTodo(@PathVariable Long listId,
                             @Valid @ModelAttribute("newTodo") TodoRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);
            if (todoListOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
                return "redirect:/";
            }

            TodoListResponse todoList = todoListOpt.get();
            List<TodoResponse> todos = todoService.getTodos(listId);

            model.addAttribute("todoList", todoList);
            model.addAttribute("todos", todos);
            model.addAttribute("editTodoListRequest", new TodoListRequest());
            return "lists/details";
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

        if (todo.isCompleted()) {
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

    // Rename todo list
    @PostMapping("/lists/{listId}/update")
    public String updateTodoList(@PathVariable Long listId,
                                 @Valid @ModelAttribute("editTodoListRequest") TodoListRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);
            if (todoListOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
                return "redirect:/";
            }

            model.addAttribute("todoList", todoListOpt.get());
            return "lists/edit"; // Return to edit form with errors
        }

        Optional<TodoListResponse> updatedList = todoService.updateTodoList(listId, request);

        if (updatedList.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo list updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo list!");
        }

        // Redirect to the details page instead of edit page
        return "redirect:/lists/" + listId;
    }

    // Delete todo list
    @PostMapping("/lists/{listId}/delete")
    public String deleteTodoList(@PathVariable Long listId,
                                 RedirectAttributes redirectAttributes) {
        boolean deleted = todoService.deleteTodoList(listId);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo list deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete todo list!");
        }

        return "redirect:/";
    }

    // Edit todo
    @GetMapping("/lists/{listId}/todos/{todoId}/edit")
    public String editTodoForm(@PathVariable Long listId,
                               @PathVariable Long todoId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Optional<TodoResponse> todoOpt = todoService.getTodo(listId, todoId);
        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);

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

        return "todos/edit";
    }

    // Update todo
    @PostMapping("/lists/{listId}/todos/{todoId}/update")
    public String updateTodo(@PathVariable Long listId,
                             @PathVariable Long todoId,
                             @RequestParam String description,
                             RedirectAttributes redirectAttributes) {

        // Create request object manually
        TodoRequest request = new TodoRequest();
        request.setDescription(description);

        // Validate manually if needed
        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Description cannot be empty!");
            return "redirect:/lists/" + listId;
        }

        Optional<TodoResponse> updateResult = todoService.updateTodo(listId, todoId, request);

        if (updateResult.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage", "Todo updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo!");
        }

        return "redirect:/lists/" + listId;
    }

    @GetMapping("/lists/{id}/edit")
    public String editTodoList(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(id);
        if (todoListOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
            return "redirect:/";
        }

        TodoListResponse todoList = todoListOpt.get();
        TodoListRequest editRequest = new TodoListRequest();
        editRequest.setName(todoList.getName());

        model.addAttribute("todoList", todoList);
        model.addAttribute("editTodoListRequest", editRequest);
        return "lists/edit";
    }

    @GetMapping("/lists/{id}/delete-confirm")
    public String confirmDeleteTodoList(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(id);
        if (todoListOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
            return "redirect:/";
        }

        model.addAttribute("todoList", todoListOpt.get());
        return "lists/delete-confirm";
    }

    // ---- NEW TODO MANAGEMENT PAGES ----

    @GetMapping("/lists/{listId}/todos/new")
    public String newTodo(@PathVariable Long listId, Model model, RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);
        if (todoListOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
            return "redirect:/";
        }

        model.addAttribute("todoList", todoListOpt.get());
        model.addAttribute("newTodo", new TodoRequest());
        return "todos/new";
    }

    @GetMapping("/lists/{listId}/todos/{todoId}/delete-confirm")
    public String confirmDeleteTodo(@PathVariable Long listId, @PathVariable Long todoId,
                                    Model model, RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);
        Optional<TodoResponse> todoOpt = todoService.getTodo(listId, todoId);

        if (todoListOpt.isEmpty() || todoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo or list not found!");
            return "redirect:/lists/" + listId;
        }

        model.addAttribute("todoList", todoListOpt.get());
        model.addAttribute("todo", todoOpt.get());
        return "todos/delete-confirm";
    }


}