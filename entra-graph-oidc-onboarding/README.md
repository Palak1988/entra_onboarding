# OIDC App Onboarding for Entra ID

This project implements a REST API for onboarding OIDC applications using the Microsoft Graph API. It is built with Spring Boot and Maven.

## Project Structure

```
entra-graph-oidc-onboarding
└── entra-graph-oidc-onboarding
    ├── src
    │   ├── main
    │   │   ├── java
    │   │   │   └── com
    │   │   │       └── example
    │   │   │           └── oidc
    │   │   │               ├── OidcOnboardingApplication.java
    │   │   │               ├── controller
    │   │   │               │   └── AppOnboardingController.java
    │   │   │               ├── service
    │   │   │               │   ├── GraphApiService.java
    │   │   │               │   └── OnboardingService.java
    │   │   │               ├── model
    │   │   │               │   └── OidcApplication.java
    │   │   │               ├── client
    │   │   │               │   └── GraphApiClient.java
    │   │   │               └── config
    │   │   │                   ├── SecurityConfig.java
    │   │   │                   └── GraphApiConfig.java
    │   │   └── resources
    │   │       ├── application.yml
    │   │       └── application-dev.yml
    │   └── test
    │       └── java
    │           └── com
    │               └── example
    │                   └── oidc
    │                       └── service
    │                           └── OnboardingServiceTest.java
    ├── pom.xml
    └── README.md
```

## Setup Instructions

1. **Clone the repository:**
   ```
   git clone <repository-url>
   cd entra-graph-oidc-onboarding
   ```

2. **Build the project:**
   ```
   mvn clean install
   ```

3. **Configure Azure Key Vault (Recommended):**
   
   **Create an Azure Key Vault:**
   ```bash
   az keyvault create --name {your-keyvault-name} --resource-group {your-resource-group}
   ```

   **Store the client secret in Key Vault:**
   ```bash
   az keyvault secret set --vault-name {your-keyvault-name} --name client-secret --value {your-client-secret}
   ```

   **Update application.yml:**
   ```yaml
   azure:
     keyvault:
       url: https://{your-keyvault-name}.vault.azure.net/
   ```

4. **Authentication Methods (in order of preference):**
   - **Managed Identity** (Recommended for Azure resources): Automatically authenticates when running on Azure services (App Service, Container Instances, etc.)
   - **Environment Variables**: Set `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID`
   - **Azure CLI**: Run `az login` before starting the application
   - **Visual Studio**: If using Visual Studio with Azure account signed in

5. **Run the application:**
   ```
   mvn spring-boot:run
   ```
   Or for development profile:
   ```
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

6. **Access the API:**
   The API will be available at `http://localhost:8080/api/onboarding`.


## Usage

- **Create OIDC Application:**
  - Endpoint: `POST /api/onboarding/app`
  - Request Body: JSON representation of `OidcApplication`.

- **Get OIDC Application:**
  - Endpoint: `GET /api/onboarding/app/{clientId}`

## Azure Key Vault Integration

This application supports fetching the OIDC client secret from Azure Key Vault for secure credential management.

### Client Secret Retrieval

The `GraphApiConfig` class automatically attempts to fetch the client secret from Azure Key Vault. If the Key Vault is not available or configured, it will fall back to the application properties.

### Custom Secret Usage

You can use the `AzureKeyVaultSecretManager` component to fetch other secrets:

```java
@Autowired
private AzureKeyVaultSecretManager secretManager;

public void someMethod() {
    String secret = secretManager.getSecret("my-secret-name");
    String secretWithDefault = secretManager.getSecret("my-secret-name", "default-value");
    boolean exists = secretManager.secretExists("my-secret-name");
}
```

### Troubleshooting

- **Secret Not Found**: Ensure the secret exists in Key Vault with the correct name (`client-secret`)
- **Authentication Failed**: Verify Azure credentials are properly configured
- **Connection Timeout**: Check that the Key Vault URL is correct and accessible



## Dependencies

This project uses the following dependencies:
- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Test
- Spring Web Client for making HTTP requests to Microsoft Graph API
- **Spring Cloud Azure Starter Key Vault** - For Azure Key Vault integration
- **Azure Identity** - For authentication with Azure services

Key Vault dependencies enable secure credential management and support multiple authentication methods including Managed Identity, Environment Variables, and Azure CLI.

## Contributing

Feel free to submit issues or pull requests for any improvements or bug fixes.

## License

This project is licensed under the MIT License.