package com.example.oidc.Util;


import java.time.OffsetDateTime;

/**
 * This class is used for OIDC supportive methods
 */
@Component
public class OIDCUtil {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * This method is used to set redirection URL and enable or disable implicit
     * grant type
     *
     * @param application       set details in application
     * @param arrayRedirectURLs
     * @param redirectionUris   URL for oidc
     * @param isImplicit        enable or disable
     * @return return application to onboard
     */
    public void setWebApplication(Application application, String[] arrayRedirectURLs,
            Map<String, Object> redirectionUris, boolean isImplicit) {

        WebApplication webApplication = new WebApplication();

        // Case 1: existing request, only arrayRedirectURLs provided
        if (arrayRedirectURLs != null && redirectionUris == null) {
            setRedirectUrlForExistingRequest(application, arrayRedirectURLs, isImplicit, webApplication);
            return;
        }

        // Case 2: new redirectionUris map provided
        String[] spaUris = commonUtil.getStringArrayFromObject(redirectionUris.get(AppConstants.SPA));
        String[] webUris = commonUtil.getStringArrayFromObject(redirectionUris.get(AppConstants.WEB));
        String[] mobileUris = commonUtil.getStringArrayFromObject(redirectionUris.get(AppConstants.MOBILE_APP));

        // Common: configure SPA
        SpaApplication spaApplication = new SpaApplication();
        setRedirectUris(spaApplication, spaUris);
        application.setSpa(spaApplication);

        // Common: configure Public Client (mobile)
        PublicClientApplication publicClientApplication = new PublicClientApplication();
        setRedirectUris(publicClientApplication, mobileUris);
        application.setPublicClient(publicClientApplication);

        // Common: implicit grant settings
        ImplicitGrantSettings implicitGrantSettings = new ImplicitGrantSettings();
        implicitGrantSettings.setEnableAccessTokenIssuance(isImplicit);
        implicitGrantSettings.setEnableIdTokenIssuance(isImplicit);

        // Common: configure webApplication
        webApplication.setImplicitGrantSettings(implicitGrantSettings);
        setRedirectUris(webApplication, webUris);
        application.setWeb(webApplication);
    }

    /**
     *
     * @param application
     * @param arrayRedirectURLs
     * @param isImplicit
     * @param webApplication
     */
    private void setRedirectUrlForExistingRequest(Application application, String[] arrayRedirectURLs,
            boolean isImplicit, WebApplication webApplication) {

        // Configure SPA part
        SpaApplication spaApplication = new SpaApplication();
        spaApplication.setRedirectUris(isImplicit ? Arrays.asList(arrayRedirectURLs) : new ArrayList<>());
        application.setSpa(spaApplication);

        // Configure implicit grant settings
        ImplicitGrantSettings implicitGrantSettings = new ImplicitGrantSettings();
        implicitGrantSettings.setEnableAccessTokenIssuance(isImplicit);
        implicitGrantSettings.setEnableIdTokenIssuance(isImplicit);

        // Configure webApplication
        webApplication.setImplicitGrantSettings(implicitGrantSettings);
        webApplication.setRedirectUris(isImplicit ? new ArrayList<>() : Arrays.asList(arrayRedirectURLs));
        application.setWeb(webApplication);
    }

    /**
     *
     * @param application
     * @param uris
     */
    public void setRedirectUris(Object application, String[] uris) {
        List<String> redirectUris;

        if (uris != null && uris.length > 0) {
            if (application instanceof PublicClientApplication) {
                String[] resultUris = Stream.concat(Arrays.stream(uris), Arrays.stream(AppConstants.MOBILE_APP_URIS))
                        .toArray(String[]::new);
                redirectUris = Arrays.asList(resultUris);
            } else {
                redirectUris = Arrays.asList(uris);
            }
        } else {
            redirectUris = new ArrayList<>();
        }

        if (application instanceof SpaApplication) {
            ((SpaApplication) application).setRedirectUris(redirectUris);
        } else if (application instanceof WebApplication) {
            ((WebApplication) application).setRedirectUris(redirectUris);
        } else if (application instanceof PublicClientApplication) {
            ((PublicClientApplication) application).setRedirectUris(redirectUris);
        }
    }

    /**
     *
     * This method is used to assign scope for application
     *
     * @param allDelegatedScopes all scopes
     * @param scopes             specific scope for application
     * @return resource access list
     */
    public List<ResourceAccess> assignScope(ServicePrincipalCollectionResponse allDelegatedScopes, String[] scopes) {
        List<ResourceAccess> resourceAccessList = new LinkedList<>();
        for (String scope : scopes) {
            if (allDelegatedScopes.getValue() != null) {
                ResourceAccess resourceAccess = new ResourceAccess();
                resourceAccess
                        .setId(Objects.requireNonNull(allDelegatedScopes.getValue().get(0).getOauth2PermissionScopes())
                                .stream().filter(p -> Objects.equals(p.getValue(), scope)).findFirst().get().getId());
                resourceAccess.setType("Scope");
                resourceAccessList.add(resourceAccess);
            }
        }
        return resourceAccessList;
    }

    /**
     *
     * This method is used to set admin grant delegated scope
     *
     * @param servicePrincipalID to set delegated scope
     * @param resourceID         to set delegated scope
     * @param scope              to set delegated scope
     * @return OAuth2 permission grant
     */
    public OAuth2PermissionGrant setAdminGrantDelegatedScope(String servicePrincipalID, String resourceID,
            String scope) {
        OAuth2PermissionGrant oAuth2PermissionGrant = new OAuth2PermissionGrant();
        oAuth2PermissionGrant.setClientId(servicePrincipalID);
        oAuth2PermissionGrant.setConsentType("AllPrincipals");
        oAuth2PermissionGrant.setResourceId(resourceID);
        oAuth2PermissionGrant.setScope(scope);
        return oAuth2PermissionGrant;
    }

    /**
     *
     * This method is used to assign delegated scope
     *
     * @param allDelegatedScope to filter
     * @param scope             specific scope to assign
     * @return resource access list for application
     */
    public List<RequiredResourceAccess> assignDelegatedScope(ServicePrincipalCollectionResponse allDelegatedScope,
            String[] scope) {
        List<RequiredResourceAccess> requiredResourceAccessList = new LinkedList<>();
        RequiredResourceAccess requiredResourceAccess = new RequiredResourceAccess();
        requiredResourceAccess.setResourceAppId(
                OnboardingProperties.appStaticProperties.getMicrosoftGraph().getMicrosoftGraphAppId());
        requiredResourceAccess.setResourceAccess(assignScope(allDelegatedScope, scope));
        requiredResourceAccessList.add(requiredResourceAccess);
        return requiredResourceAccessList;
    }

    /**
     *
     * This method is used to set client credential
     *
     * @param displayName name
     * @return addPasswordPostRequestBody
     */
    public AddPasswordPostRequestBody addPasswordPostRequestBody(String displayName) {
        AddPasswordPostRequestBody addPasswordPostRequestBody = new AddPasswordPostRequestBody();
        PasswordCredential passwordCredential = new PasswordCredential();
        // passwordCredential.setDisplayName(displayName);
        // Reduce the validity time period to 6 months from 2 years
        passwordCredential.setEndDateTime(OffsetDateTime.now()
                .plusYears(OnboardingProperties.appStaticProperties.get...));
        passwordCredential.setEndDateTime(OffsetDateTime.now().plusDays(180L));
        return addPasswordPostRequestBody.setPasswordCredential(passwordCredential);
    }

    public void enableConfiguration(Application application) {
        ApiApplication apiApplication = new ApiApplication();
        apiApplication.setAcceptMappedClaims(true);
        apiApplication.setRequestedAccessTokenVersion(
                OnboardingProperties.appStaticProperties.getEntra().getRequestedAccessTokenVersion());
        application.setApi(apiApplication);
        application.setIsFallbackPublicClient(true);
        application.setSignInAudience(
                OnboardingProperties.appStaticProperties.getEntra().getSignInAudience());
        application.setServicePrincipalLockConfiguration(getServicePrincipalLockConfiguration());
    }

    public ServicePrincipalLockConfiguration setServicePrincipalLockConfiguration() {
        ServicePrincipalLockConfiguration servicePrincipalLockConfiguration =
                new ServicePrincipalLockConfiguration();
        servicePrincipalLockConfiguration.setEnabled(true);
        servicePrincipalLockConfiguration.setAllProperties(true);
        servicePrincipalLockConfiguration.setCredentialsWithUsageSign(true);
        servicePrincipalLockConfiguration.setCredentialsWithUsageVerify(true);
        servicePrincipalLockConfiguration.setTokenEncryptionKeyId(true);
        return servicePrincipalLockConfiguration;
    }
}