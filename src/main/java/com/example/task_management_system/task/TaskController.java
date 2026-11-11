package com.example.task_management_system.task;

import com.example.task_management_system.task.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api")
public class TaskController {


    private final TaskService taskService;

    public TaskController(TaskService taskService) {

        this.taskService= taskService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<GetTaskResponse>> getTasks(
            @RequestParam(required=false) Optional<String> author,
            @RequestParam(required=false) Optional <String> assignee
    ) {

        return ResponseEntity.status(200).body(taskService.GetTasks(author,assignee));
    }

    @PostMapping("/tasks")
    public ResponseEntity<PostTaskResponse> createTask(
            Authentication authentication,
            @Valid @RequestBody PostTaskBody requestBody
    ) {

        //change response body to include asignee wich is "none" at the moment of creation

        String email = authentication.getName();

        PostTaskRequest taskRequest = new PostTaskRequest(email, requestBody.title(), requestBody.description());

        return ResponseEntity.status(200).body(taskService.CreateTask(taskRequest));
    }

    @PutMapping("tasks/{taskId}/assign")
    public ResponseEntity<PutTaskResponse> updateTask(
            @PathVariable String taskId,
            Authentication authentication,
            @Valid @RequestBody PutTaskBody requestBody
    ) {
        String authorEmail = authentication.getName();

        PutTaskRequest putRequest = new PutTaskRequest(taskId, authorEmail, requestBody.assignee());
        System.out.println(putRequest.authorEmail()+" "+putRequest.assigneeEmail()+" id: "+putRequest.taskId());

        return ResponseEntity.status(200)
                .body(taskService.PutTask(putRequest));
    }

    @PutMapping("tasks/{taskId}/status")
    public ResponseEntity<PutTaskStatusResponse> updateTaskStatus(
            Authentication authentication,
            @PathVariable String taskId,
            @Valid @RequestBody PutTaskStatusBody body
    ){

        String authorEmail = authentication.getName();

        return ResponseEntity.status(200)
                .body(taskService.UpdateStatus(authorEmail, body.status().toString(), taskId));
    }

    @PutMapping("tasks/{taskId}/comments")
    public ResponseEntity<String> updateTask(
            Authentication authentication,
            @PathVariable String taskId,
            @Valid @RequestBody PutTaskCommentBody body
            ){

        String email = authentication.getName();

        taskService.UpdateComment(email, body.text(), taskId);

        return ResponseEntity.status(200).body("Task updated with comment");
    }


    @GetMapping("tasks/{taskId}/comments")
    public ResponseEntity<List<GetCommentsResponse>> getComments(
            @PathVariable String taskId
    ){

        return ResponseEntity.status(200).body(taskService.GetComments(taskId));

    }


}
