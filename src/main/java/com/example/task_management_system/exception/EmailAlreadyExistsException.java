package com.example.task_management_system.exception;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException (String message) {
        super(message);
    }
}
