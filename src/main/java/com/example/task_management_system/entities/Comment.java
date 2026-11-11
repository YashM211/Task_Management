package com.example.task_management_system.entities;

import com.example.task_management_system.task.Task;
import com.example.task_management_system.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name="comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String text;

    @ManyToOne
    @JoinColumn(name="task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name="author")
    private AppUser author;

    public Comment() {
    }

    public Comment(String text, Task task, AppUser author ) {
        this.text = text;
        this.task = task;
        this.author = author;
    }

    public Long getId() {return id;}
    public String getText() {return text;}
    public Task getTask() {return task;}
    public AppUser getAuthor() {return author;}

    public void setId(Long id) {this.id = id;}
    public void setText(String text) {this.text = text;}
    public void setTask(Task task) {this.task = task;}
    public void setAuthor(AppUser author) {this.author = author;}

}
