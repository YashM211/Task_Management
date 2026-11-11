package com.example.task_management_system.task.dto;

public record GetCommentsResponse(
        String id,
        String task_id,
        String text,
        String author
) {
}
