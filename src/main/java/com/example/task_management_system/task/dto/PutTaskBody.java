package com.example.task_management_system.task.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PutTaskBody(
        @NotBlank(message = "assignee email is required")
        @Pattern(
                regexp = "^(none|[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+)$",
                message = "Assignee must be a valid email or 'none'"
        )
        String assignee
) {}