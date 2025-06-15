package com.example.todo.repositories;

import com.example.todo.entities.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByTodoListId(Long todoListId);
    List<Todo> findByTodoListIdAndCompleted(Long todoListId, Boolean completed);
    Optional<Todo> findByIdAndTodoListId(Long id, Long todoListId);

    long countByTodoListId(Long todoListId);
    long countByTodoListIdAndCompleted(Long todoListId, Boolean completed);
}
