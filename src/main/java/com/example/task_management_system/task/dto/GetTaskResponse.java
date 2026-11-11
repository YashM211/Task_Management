package com.example.task_management_system.task.dto;

import java.util.List;

public record GetTaskResponse(
        String id,
        String title,
        String description,
        String status,
        String author,
        String assignee,
        int total_comments

) {
}
