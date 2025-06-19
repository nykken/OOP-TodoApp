package com.example.todo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Data
@NoArgsConstructor
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Column(nullable = false, length = 200)
    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @Column(nullable = false)
    private Boolean completed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_list_id", nullable = false)
    private TodoList todoList;

    @PrePersist
    @PreUpdate
    @PreRemove
    private void prePersistUpdateRemove() {
        System.out.println("Todo callback fired for: " + this.description);
        if (todoList != null) {
            todoList.setUpdatedAt(LocalDateTime.now());
        }
    }

    public Todo(String description, TodoList todoList) {
        this.description = description;
        this.completed = false;
        this.todoList = todoList;
    }
}
