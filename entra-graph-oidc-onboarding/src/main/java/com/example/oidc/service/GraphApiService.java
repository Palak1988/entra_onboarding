package com.example.oidc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.oidc.client.GraphApiClient;
import com.example.oidc.config.GraphApiConfig;

@Service
public class GraphApiService {

    private final GraphApiClient graphApiClient;
    private final GraphApiConfig graphApiConfig;

    @Autowired
    public GraphApiService(GraphApiClient graphApiClient, GraphApiConfig graphApiConfig) {
        this.graphApiClient = graphApiClient;
        this.graphApiConfig = graphApiConfig;
    }

    public String getAccessToken(String clientId, String clientSecret) {
        // Logic to get access token from Microsoft Identity platform
        // Placeholder implementation
        return "access_token_placeholder";
    }

    public String callGraphApi(String endpoint, String accessToken) {
        // Logic to call Microsoft Graph API
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<String> response = graphApiClient.get(endpoint, headers, String.class);
        return response.getBody();
    }
}