package com.example.loginapp.repository;

import com.example.loginapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔹 Spring Data JPA automatically implements this method based on naming convention
    Optional<User> findByEmail(String email);

    // (optional) If you want both username and email search
    Optional<User> findByUsernameOrEmail(String username, String email);
}
