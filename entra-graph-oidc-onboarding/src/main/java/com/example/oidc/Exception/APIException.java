package com.example.oidc.Exception;
import com.example.oidc.model.APIErrorCode;

import lombok.*;

@Getter
public class APIException extends RuntimeException {

    private final APIErrorCode Code;
    private final String detail;
    private final Throwable cause;

    public APIException(APIErrorCode Code, String detail, Throwable cause) {
        super(detail,cause);
        this.Code = Code;
        this.detail = detail;
        this.cause = cause;
    }
    
}
