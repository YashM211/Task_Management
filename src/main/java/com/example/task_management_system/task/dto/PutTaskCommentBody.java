package com.example.task_management_system.task.dto;

import jakarta.validation.constraints.NotBlank;

public record PutTaskCommentBody(
        @NotBlank(message = "Comment cant be null")
        String text
) {
}
