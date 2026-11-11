package com.example.task_management_system.task;


import com.example.task_management_system.entities.Comment;
import com.example.task_management_system.entities.CommentRepository;
import com.example.task_management_system.task.dto.*;
import com.example.task_management_system.user.AppUser;
import com.example.task_management_system.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public PostTaskResponse CreateTask(PostTaskRequest request){

        AppUser author= userRepository.findByEmail(request.author()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Task task = new Task(
                request.title(),
                request.description(),
                author
        );

        System.out.println(task.getAuthor().getEmail()+" "+ task.getAssignee());

        //save in repository
        taskRepository.save(task);


        return new PostTaskResponse(task.getId().toString(),task.getTitle(), task.getDescription(), task.getStatus(), task.getAuthor().getEmail(),"none");

    }

    public List<GetTaskResponse> GetTasks(Optional<String> email, Optional<String> assignee){

        List<Task> tasks;

        System.out.println(email);
        System.out.println(assignee);

        if(email.isPresent() && assignee.isPresent()){
            tasks = taskRepository.findByAuthorEmailAndAssigneeEmail(email.get().toLowerCase(), assignee.get().toLowerCase(), Sort.by(Sort.Direction.DESC, "id"));
            //tasks =taskRepository.findByAuthorEmail(email.get(), Sort.by(Sort.Direction.DESC, "id"));
        } else if (email.isPresent()) {
            tasks = taskRepository.findByAuthorEmail(email.get().toLowerCase(), Sort.by(Sort.Direction.DESC, "id"));
        } else if (assignee.isPresent()) {
            tasks = taskRepository.findByAssigneeEmail(assignee.get().toLowerCase(), Sort.by(Sort.Direction.DESC, "id"));
        } else {
            tasks = taskRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }


        return tasks.stream()
                .map(this::toTaskResponse)
                .toList();
    }


    private GetTaskResponse toTaskResponse(Task task) {
        return new GetTaskResponse(
                task.getId().toString(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getAuthor().getEmail(),
                task.getAssignee() != null ? task.getAssignee().getEmail() : "none",
                task.getComments().size()

        );
    }

    public PutTaskResponse PutTask(PutTaskRequest request) {
        //Validate email in DTO
        //check if user is allowed to asign task (User is the author of the task)


        Task task = taskRepository.findById(Long.parseLong(request.taskId())).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Task not found"
        ));

        if (!request.authorEmail().toLowerCase().equals(task.getAuthor().getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is not allowed to assign this task"
            );
        }

        String assigneeEmail;


        if (request.assigneeEmail().equals("none")){
            task.setAssignee(null);
            taskRepository.save(task);
            assigneeEmail = "none";

        }else {
            AppUser assignee = userRepository.findByEmail(request.assigneeEmail().toLowerCase()).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Assignee not found"
            ));

            task.setAssignee(assignee);
            taskRepository.save(task);
            assigneeEmail = assignee.getEmail();
        }



        return new PutTaskResponse(task.getId().toString(),task.getTitle(), task.getDescription(), task.getStatus(), task.getAuthor().getEmail(),assigneeEmail);
    }
    public PutTaskStatusResponse UpdateStatus(String email, String status,String taskId){

        Task task = taskRepository.findById(Long.parseLong(taskId)).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Task not found"
        ));

        boolean isAssignee = task.getAssignee() != null && email.equals(task.getAssignee().getEmail());

        if (!email.equals(task.getAuthor().getEmail()) && !isAssignee){

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is not allowed to modify this task"
            );
        }

        task.setStatus(status);
        taskRepository.save(task);

        return new PutTaskStatusResponse(task.getId().toString(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getAuthor().getEmail(),
                task.getAssignee() != null ? task.getAssignee().getEmail() : "none");
    }

    public void UpdateComment(String email, String text,String taskId){
        Task task = taskRepository.findById(Long.parseLong(taskId)).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Task not found"
        ));


        //search author

        Optional<AppUser> author = userRepository.findByEmail(email);


        Comment comment = new Comment( text, task , author.get());

        commentRepository.save(comment);


    }


    public List<GetCommentsResponse> GetComments(String taskId){
        Task task = taskRepository.findById(Long.parseLong(taskId)).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Task not found"
        ));

        return task.getComments().stream()
                .map(this::toCommentResponse)
                .toList();

    }

    private GetCommentsResponse toCommentResponse(Comment comment){
        return new GetCommentsResponse(
                comment.getId().toString(),
                comment.getTask().getId().toString(),
                comment.getText(),
                comment.getAuthor().getEmail()
        );
    }


}
