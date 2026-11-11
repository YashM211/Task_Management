package com.example.task_management_system.task.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PutTaskRequest(
        @NotBlank(message = "Task ID is required")
        String taskId,
        @NotBlank(message = "author email is required")
        String authorEmail,
        @NotBlank(message = "assignee email is required")
        String assigneeEmail
) {
}
