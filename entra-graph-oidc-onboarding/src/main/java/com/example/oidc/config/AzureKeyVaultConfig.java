package com.example.oidc.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for Azure Key Vault integration.
 * Provides a bean for accessing secrets from Azure Key Vault.
 */
@Configuration
public class AzureKeyVaultConfig {

    @Value("${azure.keyvault.url:}")
    private String keyVaultUrl;

    /**
     * Creates a SecretClient bean for accessing Azure Key Vault.
     * Uses DefaultAzureCredential for authentication, which supports:
     * - Environment variables (AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, AZURE_TENANT_ID)
     * - Managed Identity (when running on Azure)
     * - Azure CLI credentials
     * - Visual Studio credentials
     *
     * @return SecretClient configured with the Key Vault URL
     */
    @Bean
    public SecretClient secretClient() {
        if (keyVaultUrl == null || keyVaultUrl.isEmpty()) {
            throw new IllegalArgumentException("azure.keyvault.url must be configured");
        }

        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        return new SecretClientBuilder()
                .vaultUrl(keyVaultUrl)
                .credential(credential)
                .buildClient();
    }

    /**
     * Helper method to retrieve a secret from Azure Key Vault.
     * This can be used by other beans to fetch secrets.
     *
     * @param secretClient the SecretClient bean
     * @param secretName the name of the secret to retrieve
     * @return the secret value
     */
    public static String getSecret(SecretClient secretClient, String secretName) {
        try {
            return secretClient.getSecret(secretName).getValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve secret '" + secretName + "' from Azure Key Vault", e);
        }
    }
}
