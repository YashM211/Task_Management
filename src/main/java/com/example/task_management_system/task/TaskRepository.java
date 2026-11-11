package com.example.task_management_system.task;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAuthorId(Long authorId);

    List<Task> findByAuthorEmail(String email, Sort sort);

    List<Task> findByAssigneeEmail(String email, Sort sort);

    List<Task> findByAuthorEmailAndAssigneeEmail(String authorEmail, String assignee, Sort sort);

    List<Task> findByAuthorIdAndStatus(Long authorId, String status);


}