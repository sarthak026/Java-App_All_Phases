package com.example.loginapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LoginConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean jwtEnabled;
    private boolean samlEnabled;
    private boolean oauthEnabled;

    private String jwtUrl;
    private String samlUrl;
    private String oauthUrl;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public boolean isJwtEnabled() { return jwtEnabled; }
    public void setJwtEnabled(boolean jwtEnabled) { this.jwtEnabled = jwtEnabled; }

    public boolean isSamlEnabled() { return samlEnabled; }
    public void setSamlEnabled(boolean samlEnabled) { this.samlEnabled = samlEnabled; }

    public boolean isOauthEnabled() { return oauthEnabled; }
    public void setOauthEnabled(boolean oauthEnabled) { this.oauthEnabled = oauthEnabled; }

    public String getJwtUrl() { return jwtUrl; }
    public void setJwtUrl(String jwtUrl) { this.jwtUrl = jwtUrl; }

    public String getSamlUrl() { return samlUrl; }
    public void setSamlUrl(String samlUrl) { this.samlUrl = samlUrl; }

    public String getOauthUrl() { return oauthUrl; }
    public void setOauthUrl(String oauthUrl) { this.oauthUrl = oauthUrl; }
}
