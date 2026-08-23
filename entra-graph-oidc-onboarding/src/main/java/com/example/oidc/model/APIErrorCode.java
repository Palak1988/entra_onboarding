package com.example.oidc.model;
import lombok.*;

@AllArgsConstructor
@Getter
public enum APIErrorCode {

    INTERNAL_SERVER_ERROR("internal_server_error", "Internal server error"),
    INVALID_PARAMETER("invalid_parameter"),
    RESOURCE_NOT_FOUND("resource_not_found", "Resource not found"),
    VALIDATION_IN_PROGRESS("validation_in_progress", "Validation in progress"),
    RESOURCE_NOT_FOUND ("resource_not_found", "Resource not found"),
    UNAUTHORIZED("unauthorized", "Unauthorized access"),
    private String des;
    
}
