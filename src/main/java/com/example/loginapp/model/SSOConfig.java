package com.example.loginapp.model;

import jakarta.persistence.*;

@Entity
public class SSOConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Common toggles
    private boolean jwtEnabled;
    private boolean samlEnabled;
    private boolean oauthEnabled;

    // ✅ JWT settings
    private String jwtUrl;
    private String jwtIssuer;
    private String jwtSecret;

    // ✅ SAML settings
    private String samlUrl;
    private String samlEntityId;
    private String samlCertificate;

    // ✅ OAuth settings
    private String oauthUrl;           // Authorization URL
    private String oauthTokenUrl;      // Token endpoint
    private String oauthUserInfoUrl;   // User Info endpoint
    private String oauthClientId;
    private String oauthClientSecret;
    private String oauthRedirectUri;

    // Getters & Setters
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

    public String getJwtIssuer() { return jwtIssuer; }
    public void setJwtIssuer(String jwtIssuer) { this.jwtIssuer = jwtIssuer; }

    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }

    public String getSamlUrl() { return samlUrl; }
    public void setSamlUrl(String samlUrl) { this.samlUrl = samlUrl; }

    public String getSamlEntityId() { return samlEntityId; }
    public void setSamlEntityId(String samlEntityId) { this.samlEntityId = samlEntityId; }

    public String getSamlCertificate() { return samlCertificate; }
    public void setSamlCertificate(String samlCertificate) { this.samlCertificate = samlCertificate; }

    public String getOauthUrl() { return oauthUrl; }
    public void setOauthUrl(String oauthUrl) { this.oauthUrl = oauthUrl; }

    public String getOauthTokenUrl() { return oauthTokenUrl; }
    public void setOauthTokenUrl(String oauthTokenUrl) { this.oauthTokenUrl = oauthTokenUrl; }

    public String getOauthUserInfoUrl() { return oauthUserInfoUrl; }
    public void setOauthUserInfoUrl(String oauthUserInfoUrl) { this.oauthUserInfoUrl = oauthUserInfoUrl; }

    public String getOauthClientId() { return oauthClientId; }
    public void setOauthClientId(String oauthClientId) { this.oauthClientId = oauthClientId; }

    public String getOauthClientSecret() { return oauthClientSecret; }
    public void setOauthClientSecret(String oauthClientSecret) { this.oauthClientSecret = oauthClientSecret; }

    public String getOauthRedirectUri() { return oauthRedirectUri; }
    public void setOauthRedirectUri(String oauthRedirectUri) { this.oauthRedirectUri = oauthRedirectUri; }
}
