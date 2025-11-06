//package com.example.loginapp.service;
//
//import com.example.loginapp.model.User;
//import com.example.loginapp.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class UserService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    // ✅ Find user by username OR email
//    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
//        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
//    }
//
//    // ✅ Save user (with bcrypt password encoding)
//    public void saveUser(User user) {
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        userRepository.save(user);
//    }
//
//    // ✅ Compare raw password with encoded password
//    public boolean passwordMatches(String rawPassword, String encodedPassword) {
//        return passwordEncoder.matches(rawPassword, encodedPassword);
//    }
//
//    // ✅ Auto-register user for SSO login
//    public User autoRegisterSSOUser(String email, String name) {
//        Optional<User> existingUser = userRepository.findByUsernameOrEmail(email, email);
//        if (existingUser.isPresent()) {
//            return existingUser.get();
//        }
//
//        // Default password for SSO users (random or placeholder)
//        String defaultPassword = passwordEncoder.encode("defaultPassword123");
//
//        User newUser = new User(email, email, defaultPassword, name, "USER");
//        return userRepository.save(newUser);
//    }
//}



package com.example.loginapp.service;

import com.example.loginapp.model.User;
import com.example.loginapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Find user by username OR email
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    }

    // ✅ Save user (with bcrypt password encoding)
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER"); // default
        }
        userRepository.save(user);
    }

    // ✅ Compare raw password with encoded password
    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // ✅ Auto-register user for SSO login
    public User autoRegisterSSOUser(String email, String name) {
        Optional<User> existingUser = userRepository.findByUsernameOrEmail(email, email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Default password for SSO users
        String defaultPassword = passwordEncoder.encode("defaultPassword123");
        User newUser = new User(email, email, defaultPassword, name, "USER");
        return userRepository.save(newUser);
    }

    // ✅ Create admin manually (you can call this from CommandLineRunner or a controller)
    public void createAdminUser(String username, String email, String password, String name) {
        if (userRepository.findByUsernameOrEmail(username, email).isEmpty()) {
            User admin = new User(username, email, passwordEncoder.encode(password), name, "ADMIN");
            userRepository.save(admin);
        }
    }
}
