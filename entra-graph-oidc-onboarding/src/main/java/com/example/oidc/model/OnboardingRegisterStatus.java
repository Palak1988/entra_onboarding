package com.example.oidc.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OnboardingRegisterStatus {
    REGISTERED_SUCCESSFULLY("request has been registered successfully."),
    DELETED_SUCCESSFULLY("delete request has been registered successfully."),
    UPDATE_SUCCESSFULLY("update request has been registered successfully."),
    DUPLICATE_SUCCESSFULLY("duplicate request has been registered successfully."),
    STAGE_SUCCESSFULLY("stage updated successfully.");

    private final String message;
}
