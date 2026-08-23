package com.example.oidc.Util;


import com.microsoft.graph.models.*;

/**
 * This class is used for common logs both saml and oidc.
 */
@Slf4j
@Component
public class CommonUtil {

    @Bean
    public GraphQLScalarType json() {
        return ExtendedScalars.Json;
    }

    /**
     * This method is used to get app role id
     *
     * @param appRoles app role
     * @return app role
     */
    public String getAppRoleId(List<AppRole> appRoles) {
        for (AppRole appRole : appRoles) {
            if (Objects.equals(appRole.getDisplayName(), AppConstants.USER)) {
                return Objects.requireNonNull(appRole.getId()).toString();
            }
        }
        return null;
    }

    /**
     * This method is used to add key credential
     *
     * @param uuid     key id
     * @param type     name
     * @param keyUsage sign/verify
     * @param key      value
     * @return key credential
     */
    public KeyCredential addKeyCredential(String uuid, KeyType type, KeyUsage keyUsage, String key) {
        KeyCredential keyCredential = new KeyCredential();
        CertificateUtil certificateUtil = new CertificateUtil();
        keyCredential.setKeyId(UUID.fromString(uuid));
        keyCredential.setType(type.toString());
        keyCredential.setKeyUsage(keyUsage.toString());
        return keyCredential.setKey(Base64.getDecoder().decode(certificateUtil.removeKeyWord(key)));
    }

    /**
     * This method is used to get metadata value
     *
     * @param command all data
     * @param keyPath path
     * @return metadata string
     */
    public String getMetadataValue(OnboardingCommand command, String keyPath) {
        return String.valueOf(JsonUtil.getValueFromMap(command.getMetadata(), keyPath));
    }

    /**
     * This method is used to get string array from object
     *
     * @param objData object data
     * @return string array
     */
    public String[] getStringArrayFromObject(Object objData) {
        ArrayList<String> arrayScopes = new ArrayList<String>();
        if (objData != null) {
            arrayScopes = (ArrayList<String>) objData;
        }
        return Arrays.copyOf(arrayScopes.toArray(), arrayScopes.size(), String[].class);
    }

    /**
     * This method is used to get environment
     *
     * @param command all data
     * @return environment
     */
    public String getEnv(Map<String, Object> metadata) {
        return Objects.requireNonNull(JsonUtil.getValueFromMap(metadata,
                OnboardingProperties.requestdata.getConfig().getEntraEnvironment())).toString();
    }

    /**
     * This method is used to set Entra appIds
     *
     * @param command all data
     * @param id      object id
     * @param appId   application id
     * @param appRoleId app roll id
     */
    public void setEntraAppIds(OnboardingCommand command, String id, String appId, String appRollId) {
        command.getResult().put(OnboardingResultKey.OBJECT_ID.getKey(), id);
        command.getResult().put(OnboardingResultKey.APP_ID.getKey(), appId);
        command.getResult().put(OnboardingResultKey.APP_ROLL_ID.getKey(), appRollId);
    }

    /**
     * This method is used to app role assignment
     *
     * @param group     name
     * @param spID      service principle id
     * @param appRoleId app role id
     * @return app role assignment
     */
    public static AppRoleAssignment appRoleAssignment(String group, String spID, String appRoleId) {
        AppRoleAssignment appRoleAssignment = new AppRoleAssignment();
        appRoleAssignment.setPrincipalId(UUID.fromString(group));
        appRoleAssignment.setResourceId(UUID.fromString(spID));
        appRoleAssignment.setAppRoleId(UUID.fromString(appRoleId));
        return appRoleAssignment;
    }

    public void optionalClaims(Application application, String name, String env) {
        // Enable Group Claim Mapping Policy Start
        OptionalClaims optionalClaims = new OptionalClaims();
        List<OptionalClaim> optionalClaimList = new ArrayList<>();
        OptionalClaim optionalClaim = new OptionalClaim();
        optionalClaim.setName(OnboardingProperties.appStaticProperties.getEntra().getGroupMembershipConfig().getName());
        optionalClaim.setEssential(false);
        List<String> additionalProperties = new ArrayList<>();
        additionalProperties.add(OnboardingProperties.appStaticProperties.getEntra().getGroupMembershipConfig()
                .getAdditionalProperties());
        if (env.equals("DF_TENANT")) {
            additionalProperties.add("cloud_displayname");
        }
        optionalClaim.setAdditionalProperties(additionalProperties);
        optionalClaimList.add(optionalClaim);
        if (name.equals(AuthCategory.OIDC.name()))
            optionalClaims.setIdToken(optionalClaimList);
        else
            optionalClaims.setSaml2Token(optionalClaimList);

        application.setOptionalClaims(optionalClaims);
        application.setGroupMembershipClaims(OnboardingProperties.appStaticProperties.getEntra()
                .getGroupMembershipConfig().getGroupMembershipClaims());
    }

    /**
     * This method is used to set response content
     *
     * @param command    array
     * @param status     request status
     * @param message    request msg
     * @param errorCode  if any occurred
     * @param desc       if any occurred
     */
    public void setStatus(OnboardingCommand command, String status, String message, String errorCode, String desc) {
        command.getResult().put(OnboardingResultKey.STATUS.getKey(), status);
        command.getResult().put(OnboardingResultKey.MESSAGE.getKey(), message);
        if (Objects.nonNull(errorCode)) {
            command.getResult().put(OnboardingResultKey.ERROR_CODE.getKey(), errorCode);
            command.getResult().put(OnboardingResultKey.DESCRIPTION.getKey(), desc);
        }
    }
}