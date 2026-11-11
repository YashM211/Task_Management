package com.example.task_management_system.task;


import com.example.task_management_system.entities.Comment;
import com.example.task_management_system.user.AppUser;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String status= "CREATED";


    //ManyToOne
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @ManyToOne
    @JoinColumn(name = "assignee_id", nullable = true)
    private AppUser assignee = null;


    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();



    public Task() {
    }

    public Task(String title, String description, AppUser author ) {
        this.status = "CREATED";
        this.title = title;
        this.description = description;
        this.author = author;
        this.assignee = null;
    }


    public Long getId() {return id;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}
    public String getStatus() {return status;}
    public AppUser getAuthor() {return author;}
    public AppUser getAssignee() {return assignee;}
    public List<Comment> getComments() {return comments;}


    public void setId(Long id) {this.id = id;}
    public void setTitle(String title) {this.title = title;}
    public void setDescription(String description) {this.description = description;}
    public void setStatus(String status) {this.status = status;}
    public void setAuthor(AppUser author) {this.author = author;}
    public void setAssignee(AppUser assignee) {this.assignee = assignee;}
    public void setComments(List<Comment> comments) {this.comments = comments;}



}
