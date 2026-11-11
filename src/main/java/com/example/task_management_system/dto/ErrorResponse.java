package com.example.task_management_system.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        Map<String, String> details,
        LocalDateTime timestamp
) {
    public ErrorResponse(int status, String error, Map<String, String> details) {
        this(status, error, details, LocalDateTime.now());
    }
}