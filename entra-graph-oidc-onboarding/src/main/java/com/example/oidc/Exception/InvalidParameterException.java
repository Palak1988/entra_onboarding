package com.example.oidc.Exception;
import com.example.oidc.model.APIErrorCode;

public class InvalidParameterException extends APIException {

    public InvalidParameterException(String detail) {
        super(APIErrorCode.INVALID_PARAMETER, detail);
    }

}
