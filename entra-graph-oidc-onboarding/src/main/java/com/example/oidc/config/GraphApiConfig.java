package com.example.oidc.config;

import com.azure.security.keyvault.secrets.SecretClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GraphApiConfig {

    @Value("${graph.api.base.url}")
    private String graphApiBaseUrl;

    @Value("${graph.api.client.id}")
    private String clientId;

    @Value("${graph.api.client.secret:}")
    private String clientSecretProperty;

    private final SecretClient secretClient;

    @Autowired
    public GraphApiConfig(SecretClient secretClient) {
        this.secretClient = secretClient;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getGraphApiBaseUrl() {
        return graphApiBaseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    /**
     * Retrieves the client secret from Azure Key Vault if configured,
     * otherwise falls back to the application property.
     *
     * @return the client secret from Azure Key Vault or application properties
     */
    public String getClientSecret() {
        // First, try to fetch from Azure Key Vault
        try {
            String secret = AzureKeyVaultConfig.getSecret(secretClient, "client-secret");
            if (secret != null && !secret.isEmpty()) {
                return secret;
            }
        } catch (Exception e) {
            // Log the error but fall back to property-based secret
            System.err.println("Failed to retrieve client secret from Azure Key Vault: " + e.getMessage());
        }

        // Fall back to the application property
        if (clientSecretProperty != null && !clientSecretProperty.isEmpty() && !"your-client-secret".equals(clientSecretProperty)) {
            return clientSecretProperty;
        }

        throw new IllegalStateException("Client secret not configured in Azure Key Vault or application properties");
    }
}