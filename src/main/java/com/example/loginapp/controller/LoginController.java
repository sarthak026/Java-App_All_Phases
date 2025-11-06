package com.example.loginapp.controller;

import com.example.loginapp.model.User;
import com.example.loginapp.model.LoginConfig;
import com.example.loginapp.repository.LoginConfigRepository;
import com.example.loginapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginConfigRepository loginConfigRepository;

    /**
     * ✅ Display Login Page (with SSO configuration flags)
     */
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        LoginConfig config = loginConfigRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    LoginConfig defaultConfig = new LoginConfig();
                    defaultConfig.setJwtEnabled(true);
                    defaultConfig.setSamlEnabled(false);
                    defaultConfig.setOauthEnabled(false);
                    return defaultConfig;
                });

        model.addAttribute("loginConfig", config);
        return "login";
    }

    /**
     * ✅ Username/Password Login Authentication
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String loginInput,
                        @RequestParam("password") String password,
                        Model model) {

        User user = userService.findByUsernameOrEmail(loginInput).orElse(null);

        if (user == null || !userService.passwordMatches(password, user.getPassword())) {
            model.addAttribute("error", "Invalid username/email or password");
            return "login";
        }

        // ✅ Role-based redirection
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/"; // non-admin users
        }
    }

    /**
     * ✅ Logout (handled by Spring Security)
     */
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout=true";
    }
}
