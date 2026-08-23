package com.example.oidc.Dto;

import com.jayway.jsonpath.DocumentContext;
import lombok.Getter;
import java.util.Map;

@Getter
public class OnboardingRegisterRequestDto {
    private String trackingId;
    private Map<String, Object> parameters;

    public Request createRequest() {
        Request req = new Request();
        req.setWorkItemId(extractWorkItemId());

        DocumentContext context = JsonUtil.readContext(parameters);
        req.setExtraEnv(ExtraEnv.readVal(context));
        req.setParameters(parameters);

        // Tracking ID set to current date/time
        req.setTrackingId(SystemUtil.currentDateTime());

        return req;
    }

    public String extractWorkItemId() {
        DocumentContext context = JsonUtil.readContext(parameters);
        return WORKITEM_ID.readVal(context);
    }

    public String readEnv() {
        DocumentContext context = JsonUtil.readContext(parameters);
        return EXTRA_ENVIRONMENT.readVal(context);
    }

    public String getAuthCategory() {
        Map<String, Object> oidc = getValueObject(parameters, APP_DEFINITION, APP_INSTANCE_DEFINITION, AUTHENTICATION, OPEN_ID_ATTR);
        Map<String, Object> saml = getValueObject(parameters, APP_DEFINITION, APP_INSTANCE_DEFINITION, AUTHENTICATION, SAML_ATTR);

        if (MapUtils.isEmpty(oidc) && MapUtils.isEmpty(saml)) {
            throw new InvalidParameterException("please configure open-id or saml attributes");
        }

        if (MapUtils.isNotEmpty(oidc) && MapUtils.isNotEmpty(saml)) {
            throw new InvalidParameterException("there are more than one category in parameters");
        }

        if (MapUtils.isNotEmpty(oidc)) {
            return AuthCategory.OIDC;
        }
        return AuthCategory.SAML;
    }

    public String readInstanceId() {
        DocumentContext context = JsonUtil.readContext(parameters);
        return INSTANCE_ID.readVal(context);
    }
}
