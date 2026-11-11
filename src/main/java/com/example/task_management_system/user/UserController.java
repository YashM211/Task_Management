package com.example.task_management_system.user;

import com.example.task_management_system.user.dto.UserRequest;
import com.example.task_management_system.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService= userService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.status(200).body(userService.registerUser(userRequest));
    }


}
