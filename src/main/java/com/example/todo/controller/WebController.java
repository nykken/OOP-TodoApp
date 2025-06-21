package com.example.todo.controller;

import com.example.todo.dto.*;
import com.example.todo.services.NoteService;
import com.example.todo.services.TodoService;
import com.example.todo.util.EntityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Web controller for the todo application UI.
 * Handles web pages, forms, and user interactions.
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class WebController {

    private final TodoService todoService;
    private final NoteService noteService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("apiHref", "/swagger-ui.html");
    }

    // ===================== HOME =====================

    /**
     * Display the home page with all todo lists and notes.
     *
     * @param model the view model
     * @return home page template
     */
    @GetMapping
    public String home(Model model) {
        List<TodoListResponse> todoLists = todoService.getAllTodoLists();
        List<NoteResponse> notes = noteService.getAllNotes();

        List<DashboardItem> dashboardItems = new ArrayList<>();
        dashboardItems.addAll(todoLists);
        dashboardItems.addAll(notes);

        dashboardItems.sort((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()));

        model.addAttribute("dashboardItems", dashboardItems);
        model.addAttribute("newTodoList", new TodoListRequest());
        model.addAttribute("newNote", new NoteRequest());
        return "/pages/home";
    }

    // =====================CREATE/EDIT PAGE =====================

    /**
     * Show create new todo list form.
     *
     * @param model the view model
     * @return create page template
     */
    @GetMapping("/lists/new")
    public String newTodoList(Model model) {
        model.addAttribute("entityType", EntityType.TODOLIST);
        model.addAttribute("formObject", new TodoListRequest());
        model.addAttribute("formAction", "/lists");
        model.addAttribute("title", "Create New Todo List");
        model.addAttribute("submitLabel", "Create List");
        model.addAttribute("cancelHref", "/");
        return "/pages/create";
    }

    /**
     * Show create new todo form.
     *
     * @param model the view model
     * @param listId the parent todo list ID
     * @return create page template
     */
    @GetMapping("/lists/{listId}/todos/new")
    public String newTodo(Model model, @PathVariable Long listId) {
        model.addAttribute("entityType", EntityType.TODO);
        model.addAttribute("formObject", new TodoRequest());
        model.addAttribute("formAction", "/lists/" + listId + "/todos");
        model.addAttribute("title", "Create New Todo");
        model.addAttribute("submitLabel", "Create Todo");
        model.addAttribute("cancelHref", "/");
        return "/pages/create";
    }

    /**
     * Show create new note form.
     *
     * @param model the view model
     * @return create page template
     */
    @GetMapping("/notes/new")
    public String newNote(Model model) {
        model.addAttribute("entityType", EntityType.NOTE);
        model.addAttribute("formObject", new NoteRequest());
        model.addAttribute("formAction", "/notes");
        model.addAttribute("title", "Create New Note");
        model.addAttribute("submitLabel", "Create Note");
        model.addAttribute("cancelHref", "/");
        return "/pages/create";
    }

    /**
     * Show edit todo list form.
     *
     * @param id the todo list ID
     * @param model the view model
     * @param redirectAttributes redirect attributes for error messages
     * @return create page template or redirect if not found
     */
    @GetMapping("/lists/{id}/edit")
    public String editTodoList(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> existingListOpt = todoService.getTodoList(id);

        if (existingListOpt.isEmpty()) {
            return redirectWithError(redirectAttributes, "Todo list not found!", "/");
        }

        TodoListResponse existingList = existingListOpt.get();
        TodoListRequest form = new TodoListRequest();
        form.setName(existingList.getName());

        model.addAttribute("entityType", EntityType.TODOLIST);
        model.addAttribute("formObject", form);
        model.addAttribute("formAction", "/lists/" + id + "/update"); // Note: changed to /update
        model.addAttribute("title", "Rename Todo List");
        model.addAttribute("submitLabel", "Save Changes");
        model.addAttribute("cancelHref", "/lists/" + id);
        return "/pages/create";
    }

    /**
     * Show edit todo form.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @param model the view model
     * @param redirectAttributes redirect attributes for error messages
     * @return create page template or redirect if not found
     */
    @GetMapping("/lists/{listId}/todos/{todoId}/edit")
    public String editTodo(@PathVariable Long listId,
                           @PathVariable Long todoId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Optional<TodoResponse> todoOpt = todoService.getTodo(listId, todoId);
        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);

        if (todoOpt.isEmpty() || todoListOpt.isEmpty()) {
            return redirectWithError(redirectAttributes, "Todo or list not found!", "/lists/" + listId);
        }

        TodoResponse todo = todoOpt.get();
        TodoRequest form = new TodoRequest();
        form.setDescription(todo.getDescription());

        model.addAttribute("entityType", EntityType.TODO);
        model.addAttribute("formObject", form);
        model.addAttribute("formAction", "/lists/" + listId + "/todos/" + todoId + "/update");
        model.addAttribute("title", "Edit Todo");
        model.addAttribute("submitLabel", "Save Changes");
        model.addAttribute("cancelHref", "/lists/" + listId);
        return "/pages/create";
    }

    /**
     * Show edit note form.
     *
     * @param id the note ID
     * @param model the view model
     * @param redirectAttributes redirect attributes for error messages
     * @return create page template or redirect if not found
     */
    @GetMapping("/notes/{id}/edit")
    public String editNote(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<NoteResponse> existingNoteOpt = noteService.getNote(id);

        if (existingNoteOpt.isEmpty()) {
            return redirectWithError(redirectAttributes, "Note not found!", "/");
        }

        NoteResponse existingNote = existingNoteOpt.get();
        NoteRequest form = new NoteRequest();
        form.setBody(existingNote.getBody());
        form.setTitle(existingNote.getTitle());

        model.addAttribute("entityType", EntityType.NOTE);
        model.addAttribute("formObject", form);
        model.addAttribute("formAction", "/notes/" + id + "/update");
        model.addAttribute("title", "Edit Note");
        model.addAttribute("submitLabel", "Save Changes");
        model.addAttribute("cancelHref", "/notes/" + id);
        return "/pages/create";
    }

    // ===================== CREATE OPERATIONS =====================

    /**
     * Create a new todo list.
     *
     * @param request the todo list data
     * @param bindingResult validation results
     * @param redirectAttributes redirect attributes for error messages
     * @param model the view model
     * @return redirect to new list or home page with errors
     */
    @PostMapping("/lists")
    public String createTodoList(@Valid @ModelAttribute("newTodoList") TodoListRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            List<TodoListResponse> todoLists = todoService.getAllTodoLists();
            model.addAttribute("todoLists", todoLists);
            return "/pages/home";
        }

        TodoListResponse createdList = todoService.createTodoList(request);
        return "redirect:lists/" + createdList.getId();
    }

    /**
     * Create a new todo in a list.
     *
     * @param listId the parent todo list ID
     * @param request the todo data
     * @param bindingResult validation results
     * @param redirectAttributes redirect attributes for error messages
     * @param model the view model
     * @return redirect to list details or list page with errors
     */
    @PostMapping("/lists/{listId}/todos")
    public String createTodo(@PathVariable Long listId,
                             @Valid @ModelAttribute("newTodo") TodoRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return todoService.getTodoList(listId)
                    .map(todoList -> {
                        model.addAttribute("todoList", todoList);
                        model.addAttribute("todos", todoService.getTodos(listId));
                        model.addAttribute("editTodoListRequest", new TodoListRequest());
                        return "/pages/list-details";
                    })
                    .orElseGet(() -> redirectWithError(redirectAttributes, "Todo list not found!", "/"));
        }

        Optional<TodoResponse> createdTodo = todoService.createTodo(listId, request);
        if (createdTodo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create todo!");
        }

        return "redirect:/lists/" + listId;
    }

    /**
     * Create a new note.
     *
     * @param request the note data
     * @param bindingResult validation results
     * @param redirectAttributes redirect attributes for error messages
     * @param model the view model
     * @return redirect to new note or home page with errors
     */
    @PostMapping("/notes")
    public String createNote(@Valid @ModelAttribute("newNote") NoteRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            List<TodoListResponse> todoLists = todoService.getAllTodoLists();
            List<NoteResponse> notes = noteService.getAllNotes();
            model.addAttribute("todoLists", todoLists);
            model.addAttribute("notes", notes);
            model.addAttribute("newTodoList", new TodoListRequest());

            return "/pages/home";
        }

        NoteResponse createdNote = noteService.createNote(request);
        return "redirect:/notes/" + createdNote.getId();
    }

    // ===================== UPDATE OPERATIONS =====================

    /**
     * Rename a todo list.
     *
     * @param listId the todo list ID
     * @param request the updated todo list data
     * @param bindingResult validation results
     * @param redirectAttributes redirect attributes for error messages
     * @param model the view model
     * @return redirect to list details or create page with errors
     */
    @PostMapping("/lists/{listId}/update")
    public String updateTodoList(@PathVariable Long listId,
                                 @Valid @ModelAttribute("editTodoListRequest") TodoListRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);
            if (todoListOpt.isEmpty()) {
                return redirectWithError(redirectAttributes, "Todo list not found!", "/");
            }

            model.addAttribute("todoList", todoListOpt.get());
            return "/pages/create";
        }

        Optional<TodoListResponse> updatedList = todoService.updateTodoList(listId, request);

        if (updatedList.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo list!");
        }
        return "redirect:/lists/" + listId;
    }

    /**
     * Update a todo description.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @param description the new description
     * @param redirectAttributes redirect attributes for error messages
     * @return redirect to list details
     */
    @PostMapping("/lists/{listId}/todos/{todoId}/update")
    public String updateTodo(@PathVariable Long listId,
                             @PathVariable Long todoId,
                             @RequestParam String description,
                             RedirectAttributes redirectAttributes) {

        if (description == null || description.trim().isEmpty()) {
            return redirectWithError(redirectAttributes, "Description cannot be empty!", "/lists/" + listId);
        }

        TodoRequest request = new TodoRequest();
        request.setDescription(description);

        if (todoService.updateTodo(listId, todoId, request).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo!");
        }

        return "redirect:/lists/" + listId;
    }

    /**
     * Toggle todo completion status.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @param redirectAttributes redirect attributes for error messages
     * @return redirect to list details
     */
    @PostMapping("/lists/{listId}/todos/{todoId}/toggle")
    public String toggleTodoCompletion(@PathVariable Long listId,
                                       @PathVariable Long todoId,
                                       RedirectAttributes redirectAttributes) {

        return todoService.getTodo(listId, todoId)
                .map(todo -> {
                    Optional<TodoResponse> result = todo.isCompleted()
                            ? todoService.markTodoAsIncomplete(listId, todoId)
                            : todoService.markTodoAsComplete(listId, todoId);

                    if (result.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo!");
                    }
                    return "redirect:/lists/" + listId;
                })
                .orElseGet(() -> redirectWithError(redirectAttributes, "Todo not found!", "/lists/" + listId));
    }

    /**
     * Update a note.
     *
     * @param noteId the note ID
     * @param request the updated note data
     * @param bindingResult validation results
     * @param redirectAttributes redirect attributes for error messages
     * @param model the view model
     * @return redirect to note details or create page with errors
     */
    @PostMapping("/notes/{noteId}/update")
    public String updateNote(@PathVariable Long noteId,
                             @Valid @ModelAttribute("editNoteRequest") NoteRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return noteService.getNote(noteId)
                    .map(note -> {
                        model.addAttribute("note", note);
                        return "/pages/create";
                    })
                    .orElseGet(() -> redirectWithError(redirectAttributes, "Note not found!", "/"));
        }

        Optional<NoteResponse> updatedNote = noteService.updateNote(noteId, request);
        if (updatedNote.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update note!");
        }
        return "redirect:/notes/" + noteId;
    }

    // ===================== DELETE OPERATIONS =====================

    /**
     * Delete a todo.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @param redirectAttributes redirect attributes for error messages
     * @return redirect to list details
     */
    @PostMapping("/lists/{listId}/todos/{todoId}/delete")
    public String deleteTodo(@PathVariable Long listId,
                             @PathVariable Long todoId,
                             RedirectAttributes redirectAttributes) {

        if (!todoService.deleteTodo(listId, todoId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete todo!");
        }
        return "redirect:/lists/" + listId;
    }

    /**
     * Delete a todo list.
     *
     * @param listId the todo list ID
     * @param redirectAttributes redirect attributes for error messages
     * @return redirect to home page
     */
    @PostMapping("/lists/{listId}/delete")
    public String deleteTodoList(@PathVariable Long listId,
                                 RedirectAttributes redirectAttributes) {
        if (!todoService.deleteTodoList(listId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete todo list!");
        }
        return "redirect:/";
    }

    /**
     * Delete a note.
     *
     * @param noteId the note ID
     * @param redirectAttributes redirect attributes for error messages
     * @return redirect to home page
     */
    @PostMapping("/notes/{noteId}/delete")
    public String deleteNote(@PathVariable Long noteId,
                             RedirectAttributes redirectAttributes) {
        if (!noteService.deleteNote(noteId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete note!");
        }
        return "redirect:/";
    }

    // ===================== CONFIRM DELETE PAGE =====================

    /**
     * Show delete confirmation page for a todo list.
     *
     * @param id the todo list ID
     * @param model the view model
     * @param redirectAttributes redirect attributes for error messages
     * @return delete confirmation page or redirect if not found
     */
    @GetMapping("/lists/{id}/delete-confirm")
    public String confirmDeleteTodoList(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return todoService.getTodoList(id)
                .map(todoList -> {
                    model.addAttribute("entityType", EntityType.TODOLIST);
                    model.addAttribute("entityName", todoList.getName());
                    model.addAttribute("title", "Delete Todo List");
                    model.addAttribute("cancelHref", "/");
                    model.addAttribute("formAction", "/lists/" + id + "/delete");
                    model.addAttribute("submitLabel", "Yes, Delete");
                    return "/pages/delete-confirm";
                })
                .orElseGet(() -> redirectWithError(redirectAttributes, "Todo list not found!", "/"));
    }

    /**
     * Show delete confirmation page for a todo.
     *
     * @param listId the todo list ID
     * @param todoId the todo ID
     * @param model the view model
     * @param redirectAttributes redirect attributes for error messages
     * @return delete confirmation page or redirect if not found
     */
    @GetMapping("/lists/{listId}/todos/{todoId}/delete-confirm")
    public String confirmDeleteTodo(@PathVariable Long listId, @PathVariable Long todoId,
                                    Model model, RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);
        Optional<TodoResponse> todoOpt = todoService.getTodo(listId, todoId);

        if (todoListOpt.isEmpty() || todoOpt.isEmpty()) {
            return redirectWithError(redirectAttributes, "Todo or list not found!", "/lists/" + listId);
        }

        model.addAttribute("entityType", EntityType.TODO);
        model.addAttribute("entityName", todoOpt.get().getDescription());
        model.addAttribute("parentName", todoListOpt.get().getName());
        model.addAttribute("title", "Delete Todo");
        model.addAttribute("cancelHref", "/lists/" + listId);
        model.addAttribute("formAction", "/lists/" + listId + "/todos/" + todoId + "/delete");
        model.addAttribute("submitLabel", "Yes, Delete");
        return "/pages/delete-confirm";
    }

    /**
     * Show delete confirmation page for a note.
     *
     * @param id the note ID
     * @param model the view model
     * @param redirectAttributes redirect attributes for error messages
     * @return delete confirmation page or redirect if not found
     */
    @GetMapping("/notes/{id}/delete-confirm")
    public String confirmDeleteNote(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return noteService.getNote(id)
                .map(note -> {
                    model.addAttribute("entityType", EntityType.NOTE);
                    model.addAttribute("entityName", note.getTitle());
                    model.addAttribute("title", "Delete Note");
                    model.addAttribute("cancelHref", "/notes/" + id);
                    model.addAttribute("formAction", "/notes/" + id + "/delete");
                    model.addAttribute("submitLabel", "Yes, Delete");
                    return "/pages/delete-confirm";
                })
                .orElseGet(() -> redirectWithError(redirectAttributes, "Note not found!", "/notes"));
    }

    // ===================== DETAIL VIEWS =====================

    /**
     * Display todo list details with optional filtering.
     *
     * @param listId the todo list ID
     * @param filter optional filter (completed/pending)
     * @param model the view model
     * @param redirectAttributes redirect attributes for error messages
     * @return list details page or redirect if not found
     */
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
                                        .toList();
                                case "pending" -> allTodos.stream()
                                        .filter(todo -> !todo.isCompleted())
                                        .toList();
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

                    return "/pages/list-details";
                })
                .orElseGet(() -> redirectWithError(redirectAttributes, "Todo list not found!", "/"));
    }

    /**
     * Display note details.
     *
     * @param noteId the note ID
     * @param model the view model
     * @param redirectAttributes redirect attributes for error messages
     * @return note details page or redirect if not found
     */
    @GetMapping("/notes/{noteId}")
    public String viewNote(@PathVariable Long noteId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        return noteService.getNote(noteId)
                .map(note -> {
                    model.addAttribute("note", note);
                    model.addAttribute("editNoteRequest", new NoteRequest());
                    return "/pages/note-details";
                })
                .orElseGet(() -> redirectWithError(redirectAttributes, "Note not found!", "/"));
    }

    // ===================== HELPER METHOD =====================

    /**
     * Redirect with error message
     */
    private String redirectWithError(RedirectAttributes redirectAttributes, String errorMessage, String redirectUrl) {
        redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        return "redirect:" + redirectUrl;
    }
}