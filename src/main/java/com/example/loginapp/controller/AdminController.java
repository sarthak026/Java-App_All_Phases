package com.example.loginapp.controller;

import com.example.loginapp.model.SSOConfig;
import com.example.loginapp.model.User;
import com.example.loginapp.repository.SSOConfigRepository;
import com.example.loginapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SSOConfigRepository ssoConfigRepository;

    // ✅ Admin Dashboard – view all users + total count
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("totalUsers", userRepository.count());
        return "admin";
    }

    // ✅ Load SSO Settings page (JWT + OAuth)
    @GetMapping("/admin/sso-settings")
    public String ssoSettings(Model model) {
        // Always load the latest config or create a new one
        SSOConfig config = ssoConfigRepository.findTopByOrderByIdDesc();
        if (config == null) config = new SSOConfig();
        model.addAttribute("config", config);
        return "sso-settings";
    }

    // ✅ Save or update SSO Settings (JWT + OAuth)
    @PostMapping("/admin/update-sso")
    public String updateSSOSettings(@ModelAttribute SSOConfig config) {
        // If no ID exists, reuse the last record’s ID
        if (config.getId() == null && ssoConfigRepository.count() > 0) {
            config.setId(ssoConfigRepository.findTopByOrderByIdDesc().getId());
        }

        // Save updated settings (includes both JWT + OAuth)
        ssoConfigRepository.save(config);
        return "redirect:/admin/sso-settings?saved=true";
    }

    // ✅ Add new user
    @PostMapping("/admin/add-user")
    public String addUser(@RequestParam String name,
                          @RequestParam String email,
                          @RequestParam String username,
                          @RequestParam String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    // ✅ Update existing user
    @PostMapping("/admin/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam String email) {
        var user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            userRepository.save(user);
        }
        return "redirect:/admin/dashboard";
    }

    // ✅ Delete user
    @GetMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }
}
