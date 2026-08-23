package com.example.oidc.AppConstantants;

public enum OnboardingResultKey {
    OBJECT_ID,
    APP_ID,    
    SERVICE_PRINCIPAL_ID,
    CLIENT_ID,
    TENANT_ID,
    ERROR_CODE,
    CLIENT_SECRET,
    CLAIM_MAPPING_POLICY_ID,
    CLAIM_POLICY_ID,
    STATUS,
    MESSAGE,
    DESCRIPTION;

    public String getKey() {
        return name().toLowerCase();
    }

}
