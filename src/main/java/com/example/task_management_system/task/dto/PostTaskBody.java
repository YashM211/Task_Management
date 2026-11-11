package com.example.task_management_system.task.dto;

import jakarta.validation.constraints.NotBlank;

public record PostTaskBody(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Description is required")
        String description
) {
}
