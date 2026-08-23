package com.example.oidc.client;

import com.azure.security.keyvault.secrets.SecretClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility component for fetching secrets from Azure Key Vault.
 * Provides convenience methods for secret retrieval.
 */
@Component
public class AzureKeyVaultSecretManager {

    private final SecretClient secretClient;

    @Autowired
    public AzureKeyVaultSecretManager(SecretClient secretClient) {
        this.secretClient = secretClient;
    }

    /**
     * Retrieves a secret from Azure Key Vault.
     *
     * @param secretName the name of the secret to retrieve
     * @return the secret value
     * @throws RuntimeException if the secret is not found or an error occurs
     */
    public String getSecret(String secretName) {
        try {
            return secretClient.getSecret(secretName).getValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve secret '" + secretName + "' from Azure Key Vault", e);
        }
    }

    /**
     * Retrieves a secret from Azure Key Vault with a default value if not found.
     *
     * @param secretName the name of the secret to retrieve
     * @param defaultValue the default value if secret is not found
     * @return the secret value or default value
     */
    public String getSecret(String secretName, String defaultValue) {
        try {
            return secretClient.getSecret(secretName).getValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Checks if a secret exists in Azure Key Vault.
     *
     * @param secretName the name of the secret to check
     * @return true if the secret exists, false otherwise
     */
    public boolean secretExists(String secretName) {
        try {
            secretClient.getSecret(secretName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
