package com.example.oidc.model;
package com.sc.mfa.control.adoption.v1.model;

import com.jayway.jsonpath.DocumentContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Enum representing metadata properties used in onboarding
 */
@Slf4j
public enum OnboardingMetadataProperty {

    OIDC_CLAIM_ATTRIBUTES("oidcClaimAttributes"),
    OIDC_AUTH_TYPE("oidcAuthType"),
    OIDC_AUTH_GROUPS("oidcAuthGroups"),
    OIDC_AUTH_ALLOW_AUTH_GROUPS("oidcAuthAllowAuthGroups"),
    OIDC_AUTH_DISPENSATION("oidcAuthDispensation"),
    OIDC_AUTH_OVERRIDE_DEFAULT_AUTH_TYPE("oidcAuthOverrideDefaultAuthType"),
    ACCESS_TYPE("accessType"),
    TOTAL_ENVIRONMENT("totalEnvironment"),
    EMAIL_VALIDATOR_LOOKUP("emailValidatorLookup"),
    IDENTIFIER_LIST("identifierList"),
    REDIRECTION_URL("redirectionUrl"),
    OIDC_GRANT_TYPE("oidcGrantType"),
    CLAIM_POLICY_GROUP("claimPolicyGroup"),
    USER_CONSENT_REQUIRED("userConsentRequired"),
    APP_DEFINITION("appDefinition"),
    APP_INSTANCE_ID("appInstanceId"),
    APP_INSTANCE_LOB("appInstanceLob"),
    APP_INSTANCE_ENVIRONMENT("appInstanceEnvironment"),
    APP_INSTANCE_DEPLOYMENT_TYPE("appInstanceDeploymentType"),
    SAM_AUTH_ATTRIBUTES("samAuthAttributes"),
    SAM_AUTH_ASSERTION("samAuthAssertion"),
    SAM_AUTH_ENCRYPTED_ASSERTION("samAuthEncryptedAssertion"),
    SAM_AUTH_REQUEST_SIGNING_CERT("samAuthRequestSigningCert");

    private final String key;
    private final String jsonPath;

    OnboardingMetadataProperty(String key) {
        this.key = key;
        this.jsonPath = key;
    }

    /**
     * Read value from JSON context
     */
    public <T> T readVal(DocumentContext context) {
        try {
            return context.read(jsonPath);
        } catch (Exception e) {
            log.debug("failed to read value with path {}", jsonPath, e);
            return null;
        }
    }

    public String getKey() {
        return key;
    }

    public String getJsonPath() {
        return jsonPath;
    }
}
    public <T> T readVal(DocumentContext context) {
        try {
            return context.read(jsonPath);
        } catch (Exception e) {
            log.debug("failed to read value with path {}", jsonPath, e);
            return null;
        }
    }

    public void verifyString(DocumentContext context) {
        String value = readVal(context);
        if (StringUtils.isBlank(value)) {
            throw new InvalidParameterException(String.format("%s should not be blank", getKey()));
        }
    }

    public String getValString(Map metadata) {
        return StringUtils.trimToEmpty(MapUtils.getString(metadata, key));
    }

    public static <T> T getValObject(Map meta, OnboardingMetadataProperty... properties) {
        if (properties.length == 1) {
            return getVal(meta, properties[0]);
        }

        Map data = meta;
        for (int i = 0; i < properties.length - 1; i++) {
            data = MapUtils.getMap(data, properties[i].getKey(), Collections.emptyMap());
        }

        return getVal(data, properties[properties.length - 1]);
    }

    private static <T> T getVal(Map map, OnboardingMetadataProperty property) {
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        return (T) map.get(property.getKey());
    }
}
