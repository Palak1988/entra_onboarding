package com.example.oidc.service.interationTypes;
package com.sc.mfa.control.adoption.v1.service.integrationTypes;


import static com.sc.mfa.control.adoption.common.OnboardingResultKey.CLIENT_SECRET;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.requests.GraphServiceClient;
import com.nimbusds.jose.jwk.KeyType;
import com.microsoft.graph.models.Application;
import com.microsoft.graph.models.OAuth2PermissionGrant;
import com.microsoft.graph.models.PermissionScope;
import com.microsoft.graph.models.PreAuthorizedApplication;
import com.microsoft.graph.models.RequiredResourceAccess;
import com.microsoft.graph.models.ResourceAccess;
import com.microsoft.graph.models.ServicePrincipal;
import com.microsoft.graph.models.ServicePrincipalCollectionResponse;

import java.util.*;

import org.bouncycastle.asn1.x509.KeyUsage;
import org.json.JSONArray;

@Slf4j
@Service
public class OIDCService extends GraphService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private OIDCUtil oidcUtils;

    /**
     * @param command
     * @param graphServiceClient
     */
    public void onboardApplication(OnboardingCommand command, GraphServiceClient graphServiceClient) {
        log.info("Onboarding the application");
        this.graphServiceClient = graphServiceClient;

        Application application = new Application();
        application.setMetadata(command.getRequestMetadata().getData());

        Application application2 = new Application();
        application2.setDisplayName(commonUtil.getMetadataValue(
                command,
                OnboardingProperties.requestMetadata.getCommon().getDisplayName()
        ));

        if (Objects.isNull(commonUtil.get(command.getOnboardingResultKey().OBJECT_ID.getKey()))) {
            application2.setDisplayName(commonUtil.getMetadataValue(
                    command,
                    OnboardingProperties.requestMetadata.getCommon().getDisplayName()
            ));
        }

        // Set configuration
        oidcUtils.enableConfiguration(application);
        commonUtil.optionalClaims(application, AuthCategory.OIDC.name(), command.getEnv());

        // Manifest S. NO = Redirect URL Mandatory & Manifest S. NO = Grant type
        JSONArray redirectUrls = JsonUtil.getValueFromMap(command.getMetadata(), oidcProp.getRedirectionUrl());
        String[] arrayRedirectURLs = null;
        Map<String, Object> redirectURLMap = null;

        if (redirectUrls instanceof JSONArray) {
            redirectURLMap = (Map<String, Object>) redirectUrls;
        } else if (redirectUrls instanceof ArrayList) {
            ArrayList<String> redirectUrlsList = (ArrayList<String>) redirectUrls;
            arrayRedirectURLs = (String[]) Objects.requireNonNull(redirectUrlsList)
                    .toArray(new String[redirectUrlsList.size()]);
        }

        oidcUtils.setApplication(
                application,
                arrayRedirectURLs,
                redirectURLMap,
                AppConstants.IMPLICIT.toLowerCase().equals(
                        JsonUtil.getValueFromMap(command.getMetadata(), oidcProp.getGrantType())
                                .toString()
                                .toLowerCase()
                )
        );

        // Assign delegated scopes
        HashMap<String, Object> delegatedScopes =
                (HashMap<String, Object>) JsonUtil.getValueFromMap(command.getMetadata(), oidcProp.getDelegatedScopes());

ServicePrincipalCollectionResponse allDelegatedScopes = new ServicePrincipalCollectionResponse();
if (!ObjUtils.isNull(objScopes) && objScopes.isEmpty() && objScopes.containsKey(AppConstants.SCOPE)) {
    allDelegatedScopes = getServicePrincipals(AppFilterRules.getFilterForServicePrincipalRequest(
        OnboardingProperties.getOnboardingProperties.getEntryData().getFilterExpressions()
            .getEqualityOperators().get("equals"),
        new String[] { AppFilterRules.attributes.id.toString() },
        AppFilterRules.attributes.onboard2hPermissionsScopes.toString() )));
    application.setRequiredResourceAccess(oidcUtils.assignDelegatedScope(allDelegatedScopes, scopes));
}

// Manifest S.No 4 - MTLS
if (isNewApp && ObjUtils.isNull(objScopes)) {
    Objects.requireNonNull(JsonUtil.getValueFromMap(command.getMetadata(), oidcProp.getAuthType()).toString()
        .toLowerCase());
    application.setPasswordCredentials(new ArrayList<>());
} else {
    application.setKeyCredentials(new LinkedList<>());
}

// GS S.No 2: Create or update application call
Application gsApplication = postOrPatchApplication(isNewApp, application,
    String.valueOf(command.getResult().get(OnboardingResultKey.OBJECT_ID.getKey())));
createOrUpdateServicePrincipal(command, gsApplication, isNewApp);

// Scopes admin consent
if (!ObjUtils.isNull(objScopes) && objScopes.isEmpty() && objScopes.containsKey(AppConstants.SCOPE)) {
    setDelegatedAdminGrant(allDelegatedScopes, isNewApp,
        String.valueOf(command.getResult().get(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey())), scopes);
}

// Manifest S.No 4 - Client Secret
if (isNewApp && AuthType.CLIENT_SECRET_POST.getStr().equals(authType.toUpperCase())) {
    PasswordCredentialResult = createClientSecret();
    gsApplication = gsApplication.getId();
    oidcUtils.addPasswordCredentials(getMetadataValue(command,
        String.valueOf(command.getResult().get(OnboardingResultKey.OBJECT_ID.getKey()))),
        command.getResult().put(ClientSecretText.getText()));
}

// Add Extra IDs in result
if (!ObjUtils.isNull(command.getResult().get(OnboardingResultKey.OBJECT_ID.getKey())) && isNewApp) {
    command.getResult().put(AppIdProps.toString(), gsApplication.getAppId().toString());
}

// Set the client id if we have redirect url for mobile app
if (redirectURLApp != null) {

    if (redirectURLMap != null && isNewApp) {
    if (isApplication != null && isNewApp) {
        String clientId = isApplication.getAppId();
        String appId = isApplication.getId();
        String[] mobileUris = commonUtil.getStringArrayFromObject(redirectURLMap.get(AppConstants.MOBILE_APP));
        if (clientId != null && mobileUris != null && mobileUris.length > 0) {
            setClientID(appId, clientId);
        }
    } else {
        String appId = String.valueOf(command.getResult().get(OnboardingResultKey.OBJECT_ID.getKey()));
        Application app = fetchApplication(appId);
        String clientId = app.getAppId();
        boolean isPresent = checkClientIDInTeradataApp(clientId);
        String[] mobileUris = commonUtil.getStringArrayFromObject(redirectURLMap.get(AppConstants.MOBILE_APP));
        if (mobileUris.length == 0 && isPresent) {
            removeClientIDAndPermission(appId, clientId);
        } else if (mobileUris.length > 0) {
            if (isPresent) {
                setClientID(appId, clientId);
            }
            checkAndSetAPIPermission(appId);
        }
    }
}

command.getResult().put(OnboardingResultKey.APP_ROLL_ID.getKey(),
    OnboardingProperties.appSgtticProperties.getMicrosoftGraph().getAppRoleId());
log.info("OIDC application onboarding completed successfully");

/**
 * @param appId
 * @param clientId
 */
private void checkAndSetAPIPermission(String appId) {
    boolean flag = false;
    Application teradataApp = fetchTeradataApplication();
    Application app = fetchApplication(appId);
    if (teradataApp != null && appId != null) {
        List<RequiredResourceAccess> requiredResourceAccesses = app.getRequiredResourceAccesses();
        for (RequiredResourceAccess access : requiredResourceAccesses) {
            if (teradataApp.getAppId().equals(access.getResourceAppId())) {
                flag = true;
                break;
            }
        }
    }
    if (!flag) {
        List<RequiredResourceAccess> resourceAccesses = new ArrayList<>();
        List<PermissionScope> permissionScopes = fetchApplicationPermission(teradataApp.getId());
        for (PermissionScope scope : permissionScopes) {
        }
    }
}
for (PermissionScope scope : permissionScopes) {
    ResourceAccess resourceAccess = new ResourceAccess();
    resourceAccess.setType("Scope");
    resourceAccess.setId(scope.getId());
    resourceAccesses.add(resourceAccess);
}
if (resourceAccesses != null && !resourceAccesses.isEmpty()) {
    RequiredResourceAccess requiredResourceAccesses = new RequiredResourceAccess();
    requiredResourceAccesses.setResourceAccesses(resourceAccesses);
    Application appToPatch = new Application();
    appToPatch.setRequiredResourceAccess(List.of(requiredResourceAccesses));
    patchApplication(appToPatch, appId);
}

/**
 * @param clientId
 */
private void removeClientIDAndPermission(String appId, String clientId) {
    Application teradataApp = fetchTeradataApplication();
    List<PreAuthorizedApplication> preApps = fetchUpdatedPreAuthorizedApplications(teradataApp.getId(), clientId);
    Application applicationTeradata = new Application();
    applicationTeradata.setPreAuthorizedApplications(preApps);
    patchClientID(applicationTeradata, teradataApp.getId());
}

/**
 * @param clientId
 */
private boolean checkClientIDTeradataApp(String clientId) {
    Application teradataApp = fetchTeradataApplication();
    List<PreAuthorizedApplication> preApps = fetchExistingPreAuthorizedApplications(teradataApp.getId());
    for (PreAuthorizedApplication app : preApps) {
        if (clientId.equals(app.getAppId())) {
            return true;
        }
    }
    return false;
}

/**
 * @param command
 * @param application
 * @param isNewApp
 */
private void createOrUpdateServicePrincipal(OnboardingCommand command, Application application, boolean isNewApp) {
private void createOrUpdateServicePrincipal(OnboardingCommand command, Application application, boolean isNewApp) {
    List<String> groupList = (ArrayList) JsonUtil.getValueFromMap(command.getMetadata(),
        OnboardingProperties.requestMetadata.getGroup(), getAuthAdGroups());
    boolean isEnableAppRoleAssignmentRequired = !groupList.isEmpty();
    if (isEnableAppRoleAssignmentRequired) {
        ServicePrincipal gsServicePrincipal = servicePrincipalDetails(isEnableAppRoleAssignmentRequired, isNewApp,
            application);
        String valueOf(command.getResult().get(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey()));
        command.getResult()
            .put(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey(), Objects.isNull(gsServicePrincipal)
                ? String.valueOf(command.getResult().get(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey()))
                : gsServicePrincipal.getId());
    }
}

/**
 * @param application
 * @param certificate
 */
public void setMTLSCertificate(Application application, String certificate) {
    List<KeyCredential> keyCredentials = new LinkedList<>();
    keyCredentials.add(commonUtil.addKeyCredential(UUID.randomUUID().toString(), KeyType.AsymmetricX509Cert,
        KeyUsage.Verify, certificate));
    application.setKeyCredentials(keyCredentials);
}

/**
 * @param isEnableAppRoleAssignmentRequired
 * @param isNewApp
 * @param servicePrincipalId
 * @param application
 */
public ServicePrincipal servicePrincipalDetails(boolean isEnableAppRoleAssignmentRequired, boolean isNewApp,
    String servicePrincipalId, Application application) {
    ServicePrincipal servicePrincipal = new ServicePrincipal();
    servicePrincipal.setAppRoleAssignmentRequired(isEnableAppRoleAssignmentRequired);
    servicePrincipal.setAppId(application.getAppId());

    // GS 3 - Create Service principal
    return postOrGetServicePrincipal(isNewApp, servicePrincipal, servicePrincipalId);
}

/**
 * @param allDelegatedScopes
 * @param isNewApp
 * @param servicePrincipalId
 * @param claimAttributes
 */
public void setDelegatedAdminGrant(ServicePrincipalCollectionResponse allDelegatedScopes, boolean isNewApp,
    String servicePrincipalId, String[] claimAttributes) {
        public void setDelegatedAdminGrant(ServicePrincipalCollectionResponse allDelegatedScopes, boolean isNewApp,
                                   String servicePrincipalId, String[] claimAttributes) {
    if (servicePrincipalId != null) {
        // Manifest Sec No 2: Admin grant delegated scope
        OAuth2PermissionGrant oAuth2PermissionGrant = oidcUtils.setAdminGrantDelegatedScope(servicePrincipalId,
                allDelegatedScopes.getValue().get(0).getId());
        String oAuth2PermissionGrantId = null;
        if (isNewApp) {
            oAuth2PermissionGrantId = getOAuth2PermissionGrantScope(servicePrincipalId);
            postOrPatchOAuth2PermissionGrant(oAuth2PermissionGrant, oAuth2PermissionGrantId);
        }
    }
}

/**
 * @param isNewApp
 * @param application
 * @param clientIdToAuthorize
 * @param clientId
 */
private void setClientID(String appId, String clientIdToAuthorize) {
    // 1. set redirect uri appending client id
    addJobRedirectUri(appId, clientIdToAuthorize);
    Application applicationTeraData = new Application();
    Application application = fetchTeraDataApplication();
    Application teraDataApplication = new Application();
    Application preAuthApplication = new Application();
    List<Application> preAuthApplications = fetchExistingPreAuthorizedApplications();
    applicationTeraData.setPreAuthorizedApplications(preAuthApplications);
    List<String> permissionIds = new ArrayList<>();
    List<String> readAllPermissionScopes = fetchApplicationPermission(teraDataApplicationID);
    PermissionScope readAllPermissionScope = null;
    for (PermissionScope scope : readAllPermissionScopes) {
        if (scope.getValue() != null && scope.getValue().equalsIgnoreCase(apiPermissionScope)) {
            readAllPermissionScope = scope;
        }
    }
    permissionIds.add(scope.getId().toString());
    appApplication.setPreAuthorizedApplications(preAuthApplications);
    applicationTeraData.setAppId(appApplication);
    patchClientID(applicationTeraData, teraDataApplicationID);
    if (readAllPermissionScope != null && readAllPermissionScope.getId() != null) {
        ResourceAccess resourceAccess = new ResourceAccess();
        resourceAccess.setType("Scope");
    }
}
for (PermissionScope scope : permissionScopes) {
    if (scope.getValue() != null && scope.getValue().equalsIgnoreCase(apiPermissionScope)) {
        readAllPermissionScope = scope;
    }

    permissionIds.add(scope.getId().toString());
}

authorizedClient.setDelegatedPermissionIds(permissionIds);
preAuthApplications.add(authorizedClient);
clientApplication.setPreAuthorizedApplications(preAuthApplications);
clientApplication.setApi(apiApplication);
patchClientApp(appId, teraDataApplicationId);
if (readAllPermissionScope != null) {
    ResourceAccess resourceAccess = new ResourceAccess();
    resourceAccess.setType("Scope");
    resourceAccess.setId(readAllPermissionScope.getId());
    requiredResourceAccesses.add(resourceAccess);
}
requiredResourceAccess.setResourceAccesses(List.of(resourceAccess));
requiredResourceAccess.setResourceAppId(teraDataApp.getAppId());
requiredResourceAccesses.add(requiredResourceAccess);
clientApplication.setRequiredResourceAccesses(List.of(requiredResourceAccess));
patchClientApp(appId, clientApplication);

/* Grant Admin Consent */
ServicePrincipal clientSp = getServicePrincipalOfApp(appId);
if (clientSp != null && teraDataClientSp != null) {
    OAuth2PermissionGrant oAuth2PermissionGrant = oidcUtils.setAdminGrantDelegatedScope(clientSp.getId(),
        teraDataClientSp.getId(), readAllPermissionScope.getValue());
    postOrPatchOAuth2PermissionGrant(oAuth2PermissionGrant, null);
}
return;
}

/**
 * @param appId
 * @param clientIdToAuthorize
 */
private void addMobileRedirectUri(String appId, String clientIdToAuthorize) {
    String redirectUri = AppConstants.MOBILE_REDIRECT_URI_PREFIX + clientIdToAuthorize
        + AppConstants.MOBILE_REDIRECT_URI_SUFFIX;
    PublicClientApplication publicClientApplication = fetchPublicClientApplication(appId);
    List<String> mobileUris = publicClientApplication.getRedirectUris();
    if (mobileUris.size() > 0) {
        mobileUris.add(redirectUri);
        Application app = new Application();
        publicClientApplication.setRedirectUris(mobileUris);
        setPublicClient(publicClientApplication);
        patchMobileAppRedirectUris(app, appId, mobileUris);
    }
}


