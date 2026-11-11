package com.example.task_management_system.task.dto;

import com.example.task_management_system.task.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PutTaskStatusBody(
        @NotNull (message = "Status is required")
        TaskStatus status
) {
}
