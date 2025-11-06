//package com.example.loginapp.controller;
//
//import com.example.loginapp.model.User;
//import com.example.loginapp.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//public class RegisterController {
//
//    @Autowired
//    private UserService userService;
//
//    @GetMapping("/register")
//    public String showRegisterPage() {
//        return "register";
//    }
//
//    @PostMapping("/register")
//    public String register(@RequestParam("username") String username,
//                           @RequestParam("email") String email,
//                           @RequestParam("password") String password,
//                           @RequestParam("name") String name,
//                           Model model) {
//
//        User existingUser = userService.findByUsernameOrEmail(username).orElse(null);
//        if (existingUser != null) {
//            model.addAttribute("error", "Username or Email already exists!");
//            return "register";
//        }
//
//        User newUser = new User(username, email, password, name);
//        userService.saveUser(newUser);
//
//        model.addAttribute("message", "Registration successful! Please login.");
//        return "login";
//    }
//}


package com.example.loginapp.controller;

import com.example.loginapp.model.User;
import com.example.loginapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("name") String name,
                           Model model) {

        // ✅ Check if user already exists by username or email
        User existingUser = userService.findByUsernameOrEmail(username).orElse(null);
        if (existingUser != null) {
            model.addAttribute("error", "Username or Email already exists!");
            return "register";
        }

        // ✅ Create new user with default role = USER
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setName(name);
        newUser.setRole("USER"); // 🟢 Default role

        userService.saveUser(newUser);

        model.addAttribute("message", "Registration successful! Please login.");
        return "login";
    }
}
