package com.example.loginapp.controller;

import com.example.loginapp.model.SSOConfig;
import com.example.loginapp.model.User;
import com.example.loginapp.repository.SSOConfigRepository;
import com.example.loginapp.repository.UserRepository;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
public class JWTController {

    private final UserRepository userRepository;
    private final SSOConfigRepository ssoConfigRepository;

    public JWTController(UserRepository userRepository, SSOConfigRepository ssoConfigRepository) {
        this.userRepository = userRepository;
        this.ssoConfigRepository = ssoConfigRepository;
    }

    // 🔹 Step 1: Redirect user to miniOrange JWT App login
    @GetMapping("/sso/login")
    public String redirectToSSO() {
        SSOConfig config = ssoConfigRepository.findTopByOrderByIdDesc();

        if (config == null || !config.isJwtEnabled() || config.getJwtUrl() == null) {
            return "redirect:/login?error=jwt_not_configured";
        }

        return "redirect:" + config.getJwtUrl();
    }

    // 🔹 Step 2: Handle JWT callback from miniOrange
    @GetMapping({"/sso/jwt/callback", "/sso/jwt/callback/**", "/sso/jwt/callback*"})
    public String handleSSOCallback(HttpServletRequest request) throws Exception {
        SSOConfig config = ssoConfigRepository.findTopByOrderByIdDesc();
        if (config == null || !config.isJwtEnabled()) {
            return "redirect:/login?error=jwt_disabled";
        }

        String jwtSecret = config.getJwtSecret();
        String idToken = request.getParameter("id_token");

        // ✅ Extract token from path if not passed as parameter
        if (idToken == null || idToken.isEmpty()) {
            String requestURI = request.getRequestURI();
            if (requestURI.contains("/sso/jwt/callback")) {
                idToken = requestURI.substring(requestURI.indexOf("/sso/jwt/callback") + "/sso/jwt/callback".length());
                if (idToken.startsWith("/")) idToken = idToken.substring(1);
            }
        }

        if (idToken == null || idToken.isEmpty()) {
            return "redirect:/login?error=missing_token";
        }

        // ✅ Verify JWT signature
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        if (!signedJWT.verify(new MACVerifier(jwtSecret))) {
            return "redirect:/login?error=invalid_signature";
        }

        var claims = signedJWT.getJWTClaimsSet();
        String email = claims.getStringClaim("email");
        String firstName = claims.getStringClaim("first_name");
        String lastName = claims.getStringClaim("last_name");
        String name = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

        if (email == null || email.isBlank()) {
            return "redirect:/login?error=invalid_token";
        }

        // ✅ Create or find user
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user = existingUser.orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email.split("@")[0]);
            newUser.setPassword("SSO_USER");
            newUser.setName(name.trim().isEmpty() ? "SSO User" : name.trim());
            return userRepository.save(newUser);
        });

        // ✅ IMPORTANT FIX:
        // Use the whole User object as principal, not just the email
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);

        // ✅ Set authentication and persist it to session
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        request.getSession().setAttribute("userEmail", user.getEmail());
        request.getSession().setAttribute("userName", user.getName());

        // ✅ Redirect to home (session will now stay authenticated)
        return "redirect:/";
    }

}
