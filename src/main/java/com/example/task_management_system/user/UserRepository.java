package com.example.task_management_system.user;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    //SELECT * FROM app_user WHERE email = ?
    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);


}
