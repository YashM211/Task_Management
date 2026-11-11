package com.example.task_management_system.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PostTokenResponse(
        @NotBlank(message = "Token is required")
        String token
) {
}
