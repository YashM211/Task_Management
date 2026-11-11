package com.example.task_management_system.user;

import com.example.task_management_system.user.dto.UserRequest;
import com.example.task_management_system.user.dto.UserResponse;
import com.example.task_management_system.exception.EmailAlreadyExistsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registerUser(UserRequest request) {
        String email = request.email().toLowerCase();
        //check if email already exists (not case sensitive)

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        //encrypt password
        String encryptedPassword = passwordEncoder.encode(request.password());

        //make user object
        AppUser user = new AppUser(
                email,
                encryptedPassword);

        //save in repository
        userRepository.save(user);


        return new UserResponse(user.getId(),user.getEmail());
    }
}
