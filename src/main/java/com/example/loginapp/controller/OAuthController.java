package com.example.loginapp.controller;

import com.example.loginapp.model.SSOConfig;
import com.example.loginapp.repository.SSOConfigRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
public class OAuthController {

    @Autowired
    private SSOConfigRepository configRepository;

    // Step 1: Redirect to miniOrange Authorization URL
    @GetMapping("/sso/oauth/login")
    public String oauthLogin() {
        Optional<SSOConfig> optionalConfig = configRepository.findAll().stream().findFirst();
        if (optionalConfig.isEmpty()) return "redirect:/admin/sso-settings";

        SSOConfig config = optionalConfig.get();

        if (!config.isOauthEnabled()) {
            return "redirect:/admin/sso-settings";
        }

        String authorizeUrl = config.getOauthUrl() + "?response_type=code"
                + "&client_id=" + config.getOauthClientId()
                + "&redirect_uri=" + URLEncoder.encode(config.getOauthRedirectUri(), StandardCharsets.UTF_8)
                + "&scope=openid%20profile%20email";

        return "redirect:" + authorizeUrl;
    }

    // Step 2: Handle Callback - Exchange code for token
    @GetMapping("/sso/oauth/callback")
    public String oauthCallback(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String error,
                                Model model) {
        if (error != null) {
            model.addAttribute("error", "OAuth Error: " + error);
            return "error";
        }

        Optional<SSOConfig> optionalConfig = configRepository.findAll().stream().findFirst();
        if (optionalConfig.isEmpty()) {
            model.addAttribute("error", "OAuth configuration not found");
            return "error";
        }

        SSOConfig config = optionalConfig.get();

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Token Exchange Request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "grant_type=authorization_code"
                    + "&code=" + code
                    + "&redirect_uri=" + URLEncoder.encode(config.getOauthRedirectUri(), StandardCharsets.UTF_8)
                    + "&client_id=" + config.getOauthClientId()
                    + "&client_secret=" + config.getOauthClientSecret();

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    config.getOauthTokenUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            JSONObject tokenJson = new JSONObject(tokenResponse.getBody());
            String accessToken = tokenJson.getString("access_token");

            // Fetch user info
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    config.getOauthUserInfoUrl(),
                    HttpMethod.GET,
                    userRequest,
                    String.class
            );

            JSONObject userInfo = new JSONObject(userResponse.getBody());

            model.addAttribute("name", userInfo.optString("name"));
            model.addAttribute("email", userInfo.optString("email"));
            model.addAttribute("userInfo", userInfo.toString(4));

            return "home";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "OAuth Exception: " + e.getMessage());
            return "error";
        }
    }

    // ✅ Optional: A test endpoint to verify configuration
    @GetMapping("/sso/oauth/test")
    public String testOAuthSettings(Model model) {
        Optional<SSOConfig> optionalConfig = configRepository.findAll().stream().findFirst();
        if (optionalConfig.isEmpty()) {
            model.addAttribute("error", "No OAuth configuration found!");
            return "error";
        }

        SSOConfig config = optionalConfig.get();
        model.addAttribute("config", config);
        return "login";
    }
}
