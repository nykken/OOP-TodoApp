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

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class WebController {

    private final TodoService todoService;
    private final NoteService noteService;

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


    // TODO DELETE THIS
    @GetMapping("/test")
    public String testThymeleaf(Model model) {
        List<TodoListResponse> todoLists = todoService.getAllTodoLists();
        model.addAttribute("todoLists", todoLists);
        model.addAttribute("newTodoList", new TodoListRequest());
        return "test";
    }



    // ===================== CREATE NEW ==================
    @GetMapping("/lists/new")
    public String newTodoList(Model model) {
        model.addAttribute("entityType", EntityType.TODOLIST);
        model.addAttribute("formObject", new TodoListRequest());
        model.addAttribute("formAction", "/lists");
        model.addAttribute("title", "Create New Todo List");
        model.addAttribute("submitLabel", "Create List");
        model.addAttribute("cancelHref", "/");
        return "pages/create";
    }

    @GetMapping("/lists/{listId}/todos/new")
    public String newTodo(Model model, @PathVariable Long listId) {
        model.addAttribute("entityType", EntityType.TODO);
        model.addAttribute("formObject", new TodoRequest());
        model.addAttribute("formAction", "/lists/" + listId + "/todos");
        model.addAttribute("title", "Create New Todo");
        model.addAttribute("submitLabel", "Create Todo");
        model.addAttribute("cancelHref", "/");
        return "pages/create";
    }

    @GetMapping("/notes/new")
    public String newNote(Model model) {
        model.addAttribute("entityType", EntityType.NOTE);
        model.addAttribute("formObject", new NoteRequest());
        model.addAttribute("formAction", "/notes");
        model.addAttribute("title", "Create New Note");
        model.addAttribute("submitLabel", "Create Note");
        model.addAttribute("cancelHref", "/");
        return "pages/create";
    }


    // ========= EDIT ===========
    @GetMapping("/lists/{id}/edit")
    public String editTodoList(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> existingListOpt = todoService.getTodoList(id);

        if (existingListOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
            return "redirect:/";
        }

        TodoListResponse existingList = existingListOpt.get();
        TodoListRequest form = new TodoListRequest();
        form.setName(existingList.getName());

        model.addAttribute("entityType", EntityType.TODOLIST);
        model.addAttribute("formObject", form);
        model.addAttribute("formAction", "/lists/" + id + "/update"); // Note: changed to /update
        model.addAttribute("title", "Edit Todo List");
        model.addAttribute("submitLabel", "Save Changes");
        model.addAttribute("cancelHref", "/lists/" + id);
        return "pages/create";
    }

    @GetMapping("/lists/{listId}/todos/{todoId}/edit")
    public String editTodo(@PathVariable Long listId,
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
        TodoRequest form = new TodoRequest();
        form.setDescription(todo.getDescription());

        model.addAttribute("entityType", EntityType.TODO);
        model.addAttribute("formObject", form);
        model.addAttribute("formAction", "/lists/" + listId + "/todos/" + todoId + "/update");
        model.addAttribute("title", "Edit Todo");
        model.addAttribute("submitLabel", "Save Changes");
        model.addAttribute("cancelHref", "/lists/" + listId);
        return "pages/create";
    }

    @GetMapping("/notes/{id}/edit")
    public String editNote(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<NoteResponse> existingNoteOpt = noteService.getNote(id);

        if (existingNoteOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Note not found!");
            return "redirect:/";
        }

        NoteResponse existingNote = existingNoteOpt.get();
        NoteRequest form = new NoteRequest();
        form.setBody(existingNote.getBody());

        model.addAttribute("entityType", EntityType.NOTE);
        model.addAttribute("formObject", form);
        model.addAttribute("formAction", "/notes/" + id + "/update");
        model.addAttribute("title", "Edit Note");
        model.addAttribute("submitLabel", "Save Changes");
        model.addAttribute("cancelHref", "/notes/" + id);
        return "pages/create";
    }


    @PostMapping("/lists")
    public String createTodoList(@Valid @ModelAttribute("newTodoList") TodoListRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            List<TodoListResponse> todoLists = todoService.getAllTodoLists();
            model.addAttribute("todoLists", todoLists);
            return "pages/home";
        }

        TodoListResponse createdList = todoService.createTodoList(request);
        return "redirect:lists/" + createdList.getId();
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

                    return "/pages/lists/details";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
                    return "redirect:/";
                });
    }

    // Create new todo in a list
    @PostMapping("lists/{listId}/todos")
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
                        return "/pages/lists/details";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
                        return "redirect:/";
                    });
        }

        Optional<TodoResponse> createdTodo = todoService.createTodo(listId, request);
        if (createdTodo.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create todo!");
        }

        return "redirect:/lists/" + listId;
    }

    // Toggle todo completion status
    @PostMapping("lists/{listId}/todos/{todoId}/toggle")
    public String toggleTodoCompletion(@PathVariable Long listId,
                                       @PathVariable Long todoId,
                                       RedirectAttributes redirectAttributes) {

        return todoService.getTodo(listId, todoId)
                .map(todo -> {
                    var result = todo.isCompleted()
                            ? todoService.markTodoAsIncomplete(listId, todoId)
                            : todoService.markTodoAsComplete(listId, todoId);

                    if (result.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo!");
                    }
                    return "redirect:/lists/" + listId;
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Todo not found!");
                    return "redirect:/lists/" + listId;
                });
    }

    // Delete todo
    @PostMapping("lists/{listId}/todos/{todoId}/delete")
    public String deleteTodo(@PathVariable Long listId,
                             @PathVariable Long todoId,
                             RedirectAttributes redirectAttributes) {

        if (!todoService.deleteTodo(listId, todoId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete todo!");
        }
        return "redirect:/lists/" + listId;
    }

    // Rename todo list
    @PostMapping("lists/{listId}/update")
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
            return "pages/lists/edit"; // Return to edit form with errors
        }

        Optional<TodoListResponse> updatedList = todoService.updateTodoList(listId, request);

        if (updatedList.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo list!");
        }
        // Redirect to the details page instead of edit page
        return "redirect:/lists/" + listId;
    }

    // Delete todo list
    @PostMapping("/lists/{listId}/delete")
    public String deleteTodoList(@PathVariable Long listId,
                                 RedirectAttributes redirectAttributes) {
        if (!todoService.deleteTodoList(listId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete todo list!");
        }
        return "redirect:/";
    }


    // Edit todo
//    @GetMapping("lists/{listId}/todos/{todoId}/edit")
//    public String editTodoForm(@PathVariable Long listId,
//                               @PathVariable Long todoId,
//                               Model model,
//                               RedirectAttributes redirectAttributes) {
//        Optional<TodoResponse> todoOpt = todoService.getTodo(listId, todoId);
//        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);
//
//        if (todoOpt.isEmpty() || todoListOpt.isEmpty()) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Todo or list not found!");
//            return "redirect:/lists/" + listId;
//        }
//
//        TodoResponse todo = todoOpt.get();
//        TodoRequest editRequest = new TodoRequest();
//        editRequest.setDescription(todo.getDescription());
//
//        model.addAttribute("todoList", todoListOpt.get());
//        model.addAttribute("todo", todo);
//        model.addAttribute("editTodoRequest", editRequest);
//
//        return "pages/todos/edit";
//    }

    // Update todo
    @PostMapping("lists/{listId}/todos/{todoId}/update")
    public String updateTodo(@PathVariable Long listId,
                             @PathVariable Long todoId,
                             @RequestParam String description,
                             RedirectAttributes redirectAttributes) {

        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Description cannot be empty!");
            return "redirect:/lists/" + listId;
        }

        var request = new TodoRequest();
        request.setDescription(description);

        if (todoService.updateTodo(listId, todoId, request).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update todo!");
        }

        return "redirect:/lists/" + listId;
    }

//    @GetMapping("/lists/{id}/edit")
//    public String editTodoList(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
//        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(id);
//        if (todoListOpt.isEmpty()) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
//            return "redirect:/";
//        }
//
//        TodoListResponse todoList = todoListOpt.get();
//        TodoListRequest editRequest = new TodoListRequest();
//        editRequest.setName(todoList.getName());
//
//        model.addAttribute("todoList", todoList);
//        model.addAttribute("editTodoListRequest", editRequest);
//        return "pages/lists/edit";
//    }

    @GetMapping("/lists/{id}/delete-confirm")
    public String confirmDeleteTodoList(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(id);
        if (todoListOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
            return "redirect:/";
        }

        model.addAttribute("todoList", todoListOpt.get());
        return "pages/lists/delete-confirm";
    }

    // ---- NEW TODO MANAGEMENT PAGES ----

//    @GetMapping("/lists/{listId}/todos/new")
//    public String newTodo(@PathVariable Long listId, Model model, RedirectAttributes redirectAttributes) {
//        Optional<TodoListResponse> todoListOpt = todoService.getTodoList(listId);
//        if (todoListOpt.isEmpty()) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Todo list not found!");
//            return "redirect:/";
//        }
//
//        model.addAttribute("todoList", todoListOpt.get());
//        model.addAttribute("newTodo", new TodoRequest());
//        return "pages/todos/new";
//    }

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
        return "pages/todos/delete-confirm";
    }

    // ===================== NOTE OPERATIONS ==================

//    @GetMapping("/notes/new")
//    public String newNote(Model model) {
//        model.addAttribute("newNote", new NoteRequest());
//        return "pages/notes/new";
//    }

    @PostMapping("/notes")
    public String createNote(@Valid @ModelAttribute("newNote") NoteRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            var todoLists = todoService.getAllTodoLists();
            var notes = noteService.getAllNotes();
            model.addAttribute("todoLists", todoLists);
            model.addAttribute("notes", notes);
            model.addAttribute("newTodoList", new TodoListRequest());
            return "home";
        }

        var createdNote = noteService.createNote(request);
        return "redirect:/notes/" + createdNote.getId();
    }

    @GetMapping("/notes/{noteId}")
    public String viewNote(@PathVariable Long noteId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        return noteService.getNote(noteId)
                .map(note -> {
                    model.addAttribute("note", note);
                    model.addAttribute("editNoteRequest", new NoteRequest());
                    return "notes/details";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Note not found!");
                    return "redirect:/";
                });
    }

//    @GetMapping("/notes/{noteId}/edit")
//    public String editNoteForm(@PathVariable Long noteId,
//                               Model model,
//                               RedirectAttributes redirectAttributes) {
//        return noteService.getNote(noteId)
//                .map(note -> {
//                    NoteRequest editRequest = new NoteRequest();
//                    editRequest.setTitle(note.getTitle());
//                    editRequest.setBody(note.getBody());
//
//                    model.addAttribute("note", note);
//                    model.addAttribute("editNoteRequest", editRequest);
//                    return "notes/edit";
//                })
//                .orElseGet(() -> {
//                    redirectAttributes.addFlashAttribute("errorMessage", "Note not found!");
//                    return "redirect:/";
//                });
//    }

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
                        return "notes/edit";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Note not found!");
                        return "redirect:/";
                    });
        }

        var updatedNote = noteService.updateNote(noteId, request);
        if (updatedNote.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update note!");
        }
        return "redirect:/notes/" + noteId;
    }

    @PostMapping("/notes/{noteId}/delete")
    public String deleteNote(@PathVariable Long noteId,
                             RedirectAttributes redirectAttributes) {
        if (!noteService.deleteNote(noteId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete note!");
        }
        return "redirect:/";
    }

    @GetMapping("/notes/{noteId}/delete-confirm")
    public String confirmDeleteNote(@PathVariable Long noteId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        return noteService.getNote(noteId)
                .map(note -> {
                    model.addAttribute("note", note);
                    return "notes/delete-confirm";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Note not found!");
                    return "redirect:/";
                });
    }


}