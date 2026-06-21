package com.example.oidc.model;

import java.util.List;

public class OidcApplication {
    private String clientId;
    private String clientSecret;
    private List<String> redirectUris;

    public OidcApplication() {
    }

    public OidcApplication(String clientId, String clientSecret, List<String> redirectUris) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUris = redirectUris;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }
}