package com.example.task_management_system.task.dto;

public record PutTaskResponse(
        String id,
        String title,
        String description,
        String status,
        String author,
        String assignee
) {
}
