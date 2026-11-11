package com.example.task_management_system.user;

import com.example.task_management_system.entities.Comment;
import com.example.task_management_system.task.Task;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name="users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role= "USER";

    @OneToMany(mappedBy = "author")
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "assignee")
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();



    public AppUser() {
    }

    public AppUser(String email, String password) {
        this.email = email;
        this.password = password;
        this.role = "USER";

    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {return role;}

    public List<Task> getTasks() {
        return tasks;
    }

    public List<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {this.role = role;}

    public void setTasks(List<Task> tasks) {this.tasks = tasks;}

    public void setAssignedTasks(List<Task> assignedTasks) {this.assignedTasks = assignedTasks;}
}
