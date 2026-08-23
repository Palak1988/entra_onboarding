package com.example.oidc.service.generic;
import static com.sc.mfa.control.adoption.configuration.OnboardingProperties.appStaticProperties;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.models.AppRoleAssignment;
import com.microsoft.graph.models.ApplicationServicePrincipal;
import com.microsoft.graph.models.ClaimsMappingPolicy;
import com.microsoft.graph.models.OAuth2PermissionGrant;
import com.microsoft.graph.models.PasswordCredential;
import com.microsoft.graph.models.PermissionScope;
import com.microsoft.graph.models.PreAuthorizedApplication;
import com.microsoft.graph.models.ServicePrincipal;
import com.microsoft.graph.requests.AppRoleAssignmentCollectionResponse;
import com.microsoft.graph.requests.ApplicationCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.ServicePrincipalCollectionResponse;
import com.microsoft.graph.requests.ServicePrincipalRequestBuilder;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

@Service
@Slf4j
@Retryable(maxAttempts = 2, backoff = @Backoff(maxDelay = 20000L, random = true),
    retryFor = Exception.class)
public class GraphService {
    public GraphServiceClient graphServiceClient;

    @Autowired
    OkHttpClient okHttpClient;

    /**
     * This method is used to create connection between MS graph and onboarding
     * @return graph session
     */
    public final GraphServiceClient connectGraphService(OnboardingCommand command, OnboardingEnvironment environment) {
        try {
            if (environment.getEnvConfig().isEnableClientSecret()) {
                final ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                    .clientId(environment.getEnvConfig().getClientId())
                    .tenantId(environment.getEnvConfig().getTenantId())
                    .clientSecret(new String(command.getMicrosoftGraphClientEntry()))
                    .httpClient(okHttpClient.newOkHttpAsyncHttpClientBuilder(okHttpClient).build()).build();
                return getGSClient(credential);
            } else {
                final ClientCertificateCredential credential = new ClientCertificateCredentialBuilder()
                    .clientId(environment.getEnvConfig().getClientId())
                    .tenantId(environment.getEnvConfig().getTenantId())
                    .pfxCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(command.getMicrosoftGraphPFX())))
                    .clientCertificatePassword(String.valueOf(command.getMicrosoftGraphPrivateKeyPass()))
                    .httpClient(okHttpClient.newOkHttpAsyncHttpClientBuilder(okHttpClient).build()).build();
                return getGSClient(credential);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public ServicePrincipalCollectionResponse getServicePrincipals(
        Consumer<ServicePrincipalRequestBuilder.GetRequestConfiguration> requestConfiguration) {
        return graphServiceClient.servicePrincipals().get(requestConfiguration);
    }

    public Application postOrPatchApplication(boolean isNewApp, Application application, String applicationId) {
        return isNewApp ? graphServiceClient.applications().post(application)
            : graphServiceClient.applications().byApplicationId(applicationId).patch(application);
    }

    public ServicePrincipal postOrGetServicePrincipal(boolean isNewApp, ServicePrincipal servicePrincipal,
    String servicePrincipalId) {
    return isNewApp ? graphServiceClient.servicePrincipals().post(servicePrincipal)
        : graphServiceClient.servicePrincipals().byServicePrincipalId(servicePrincipalId).get();
}

public GroupCollectionResponse getGroups(
    Consumer<GroupsRequestBuilder.GetRequestConfiguration> requestConfiguration) {
    return graphServiceClient.groups().get(requestConfiguration);
}

public GroupCollectionResponse getGroupsWithGraphService(GraphServiceClient gsClient,
    Consumer<GroupsRequestBuilder.GetRequestConfiguration> requestConfiguration) {
    return gsClient.groups().get(requestConfiguration);
}

public AppRoleAssignmentCollectionResponse getAppRoleAssignmentForSP(String spID) {
    return graphServiceClient.servicePrincipals().byServicePrincipalId(spID).appRoleAssignedTo().get();
}

public void deleteAppRoleAssignmentForSP(String spID, String appRoleAssignmentID) {
    graphServiceClient.servicePrincipals().byServicePrincipalId(spID).appRoleAssignedTo()
        .byAppRoleAssignmentId(appRoleAssignmentID).delete();
}

public void postAppRoleAssignment(String spID, AppRoleAssignment appRoleAssignment) {
    try {
        graphServiceClient.servicePrincipals().byServicePrincipalId(spID).appRoleAssignments()
            .post(appRoleAssignment);
    } catch (ODataError o) {
        log.error("ODataError Error : " + o.getMessage(), o.getMessage());
    } catch (Exception e) {
        log.error("Exception Error : " + e.getMessage(), e.getMessage());
    }
}

public void postOrPatchOAuth2PermissionGrant(OAuth2PermissionGrant oAuth2PermissionGrant,
    String oAuth2PermissionGrantId) {
    if (oAuth2PermissionGrantId == null) {
        graphServiceClient.oauth2PermissionGrants().post(oAuth2PermissionGrant);
    } else {
        graphServiceClient.oauth2PermissionGrants().byOAuth2PermissionGrantId(oAuth2PermissionGrantId)
            .patch(oAuth2PermissionGrant);
    }
}

public void patchClientId(Application applicationScope, String appId) {
    ApplicationItemRequestBuilder ab = graphServiceClient.applications().byApplicationId(appId);
    ab.patch(applicationScope);
}

public void patchMobileAppRedirectUris(Application application, String appId, List<String> redirectUris) {
    ApplicationItemRequestBuilder ab = graphServiceClient.applications().byApplicationId(appId);
    ab.patch(application);
}

public void patchAPIScope(Application application, String appId) {
    graphServiceClient.applications().byApplicationId(appId).patch(application);

}

public void patchIdentifierUri(Application updatedIdentifierUriApp, String appId) {
    graphServiceClient.applications().byApplicationId(appId).patch(updatedIdentifierUriApp);
}

public String getOauth2PermissionGrantScope(String spID) {
    return Objects.requireNonNull(
        graphServiceClient.servicePrincipals().byServicePrincipalId(spID).oauth2PermissionGrants().get()
        .getValue()).get(0).getScope();
}

public PasswordCredential createClientSecret(String applicationId,
    AddPasswordPostRequestBody addPasswordPostRequestBody) {
    return graphServiceClient.applications().byApplicationId(applicationId)
        .addPassword(addPasswordPostRequestBody)
        .post(addPasswordPostRequestBody);
}

public String getApplicationTemplateID(
    Consumer<ApplicationTemplatesRequestBuilder.GetRequestConfiguration> requestConfiguration) {
    return Objects.requireNonNull(
        graphServiceClient.applicationTemplates().get(requestConfiguration).getValue())
        .get(0).getId();
}

public ApplicationServicePrincipal getApplicationServicePrincipal(
    InstantiatePostRequestBody instantiatePostRequestBody, String templateId) {
    return graphServiceClient.applicationTemplates().byApplicationTemplateId(templateId)
        .instantiate(instantiatePostRequestBody)
        .post(instantiatePostRequestBody);
}

public void servicePrincipalPatch(ServicePrincipal servicePrincipal, String servicePrincipalId)
    throws InterruptedException {
    log.info("Service principal patch start :: EntraID servicePrincipalPatch :: " + servicePrincipalId);
    retryService(servicePrincipalId, "servicePrincipalPatch",
        () -> graphServiceClient.servicePrincipals().byServicePrincipalId(servicePrincipalId)
            .patch(servicePrincipal));
    log.info("Service principal patch end :: EntraID servicePrincipalPatch :: " + servicePrincipalId);
}

public ClaimsMappingPolicy createClaimsMappingPolicyPost(ClaimsMappingPolicy claimsMappingPolicy) {
    return graphServiceClient.policies().claimsMappingPolicies()
        .post(claimsMappingPolicy);
}

public void assignClaimsMappingPolicy(ReferenceCreate referenceCreate, String servicePrincipalID,
    String claimsMappingPolicyId) {
    graphServiceClient.servicePrincipals().byServicePrincipalId(servicePrincipalID)
        .claimsMappingPolicies().ref()
        .post(referenceCreate);
}

public String getClaimsMappingPolicyId(String servicePrincipalID) {
    List<ClaimsMappingPolicy> claimsMappingPolicyList = Objects.requireNonNull(
        graphServiceClient.servicePrincipals().byServicePrincipalId(servicePrincipalID)
            .claimsMappingPolicies().get().getValue());
    if (Objects.nonNull(claimsMappingPolicyList) && !claimsMappingPolicyList.isEmpty())
        return claimsMappingPolicyList.get(0).getId();
}return null;

public void deleteApplication(String id) throws InterruptedException {
    log.info("Delete application start :: EntraID Object Id :: " + id);
    retryService(id, "deleteApplication", null);
    log.info("Delete application end");
}

public void retryService(String id, String api, ServicePrincipal servicePrincipal)
        throws InterruptedException {
    log.info("Retry Service :: " + api);
    int retryCount = 0;
    while (retryCount < appStaticProperties.getMaxRetries()) {
        try {
            log.info("Retry Service :: sleep :: " + appStaticProperties.getMaxDelay() + " milliseconds");
            Thread.sleep(appStaticProperties.getMaxDelay());
            switch (api) {
                case "deleteApplication":
                    graphServiceClient.applications().byApplicationId(id).delete();
                    break;
                case "servicePrincipalPatch":
                    graphServiceClient.servicePrincipals().byServicePrincipalId(id).patch(servicePrincipal);
                    break;
            }
            break;
        } catch (Exception e) {
            log.error("RetryService :: Retry attempt :: " + retryCount + " :: Error Message" + e.getMessage());
            retryCount++;
            if (retryCount == appStaticProperties.getMaxRetries()) {
                throw e;
            } else {
                Thread.sleep(appStaticProperties.getMaxDelay());
            }
        }
    }
}

public void patchCustomSecurityAttribute(String servicePrincipalId, ServicePrincipal servicePrincipal) {
    graphServiceClient.servicePrincipals().byServicePrincipalId(String.valueOf(servicePrincipalId))
            .patch(servicePrincipal);
}

public ApplicationCollectionResponse getApplication() {
    GraphServiceApplicationsRequestBuilder requestConfiguration = requestConfiguration();
    return graphServiceClient.applications().get(requestConfiguration);
}

public GraphServiceClient getGsClient(TokenCredential credential) {
    String allowedHost[] = { "graph.microsoft.com", "login.microsoftonline.com" };
    IAuthenticationProvider authenticationProvider = new AzureIdentityAuthenticationProvider(credential,
            allowedHost);
    return new GraphServiceClient(authenticationProvider, okHttpClient);
}

public List<PermissionScope> fetchApplicationPermission(String appId) {
    return graphServiceClient.applications().byApplicationId(appId).get().getApi().getOauth2PermissionScopes();
}

public PublicClientApplication fetchPublicClientApplication(String appId) {
    return graphServiceClient.applications().byApplicationId(appId).get().getPublicClient();
}

public List<PreAuthorizedApplication> fetchExistingPreAuthorizedApplications(String appId) {
    List<PreAuthorizedApplication> finalListOfPreAuthApps = new ArrayList<>();
    List<PreAuthorizedApplication> preAuthApplicationList = graphServiceClient.applications().byApplicationId(appId)
            .get().getApi().getPreAuthorizedApplications();
    for (PreAuthorizedApplication application : preAuthApplicationList) {
        List<Application> apps = graphServiceClient.applications().get(requestConfig -> {
            requestConfig.queryParameters.filter = "appId eq '" + application.getAppId() + "'";
            requestConfig.queryParameters.select = new String[] { "id", "appId", "displayName" };
        }).getValue();
        if (apps != null && !apps.isEmpty()) {
            finalListOfPreAuthApps.add(application);
        }
    }
    return finalListOfPreAuthApps;
}

public List<PreAuthorizedApplication> fetchUpdatedPreAuthorizedApplications(String teradataAppId, String clientId) {
    List<PreAuthorizedApplication> finalListOfPreAuthApps = new ArrayList<>();
    List<PreAuthorizedApplication> preAuthApplicationList = graphServiceClient.applications()
            .byApplicationId(teradataAppId).get().getApi().getPreAuthorizedApplications();
    for (PreAuthorizedApplication application : preAuthApplicationList) {
        if (!clientId.equals(application.getAppId())) {
            finalListOfPreAuthApps.add(application);
        }
    }
    return finalListOfPreAuthApps;
}

public Application fetchApplication(String appId) {
    return graphServiceClient.applications().byApplicationId(appId).get();
}

public void patchApplication(Application appToPatch, String appId) {
    ApplicationItemRequestBuilder ab = graphServiceClient.applications().byApplicationId(appId);
    ab.patch(appToPatch);
}

/**
 *
 */
public ServicePrincipal getServicePrincipalOfApp(String appId) {
    Application app = fetchApplication(appId);
    return graphServiceClient.servicePrincipalsWithAppId(app.getAppId()).get();
}

public void removeExistingServicePrincipal(String servicePrincipalId) {
    graphServiceClient.servicePrincipals().byServicePrincipalId(servicePrincipalId).delete();
}

public Application fetchTeradataApplication() {
    List<Application> apps = graphServiceClient.applications().get(requestConfig -> {
        requestConfig.queryParameters.filter = "displayName eq '" + (AppConstants.TERADATA_APP_DISPLAY_NAME).replace("'", "''") + "'";
        requestConfig.queryParameters.select = new String[] { "id", "appId", "displayName" };
    }).getValue();
    
        return (apps.size()==1)?apps.get(0):null;
    }
}




