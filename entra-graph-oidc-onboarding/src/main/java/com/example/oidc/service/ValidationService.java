package com.example.oidc.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Validate input payload via JSON schema, the schema is fetched from config
 */
@Slf4j
@Service
public class ValidationService {

    @Resource
    private ConfigurationService configurationService;

    private final SchemaValidatorsConfig schemaConfig = SchemaValidatorsConfig.builder()
            .errorMessageKeyword("errorMessage")
            .build();

    private final JsonSchemaFactory factory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);

    /**
     * Validate payload
     * @param request request
     * @return list of error messages
     */
    public List<String> validate(Request request) {
        try {
            RequestConfig config = configurationService.findConfig("VALIDATION",
                    "Validation-" + request.getType());

            JsonSchema schema = factory.getSchema(config.getConfigData(),
                    InputFormat.JSON, schemaConfig);

            Map<String, List<String>> redirectUrlMap =
                    (Map<String, List<String>>) JsonUtil.getValueFromMap(
                            request.getMetadata(),
                            OnboardingProperties.requestMetadata.onboarding().getRedirectionUrl());

            Set<String> duplicateUrls = findDuplicateUrls(redirectUrlMap);
            if (!duplicateUrls.isEmpty()) {
                return List.of("Duplicate url error " + duplicateUrls);
            }

            Set<ValidationMessage> result = schema.validate(
                    JsonUtil.getObjectMapper().readTree(JsonUtil.toJson(request.getMetadata())));

            return result.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            log.error("validate by json schema error, request trackingId {}",
                    request.getTrackingId(), e);
            throw new APIException(APIErrorCode.INTERNAL_SERVER_ERROR, "json schema error");
        }
    }

    /**
     * Find duplicate URLs in redirection map
     * @param redirectionUrl map of redirection URLs
     * @return set of duplicate URLs
     */
    public static Set<String> findDuplicateUrls(Map<String, List<String>> redirectionUrl) {
        Set<String> seen = new HashSet<>();
        if (redirectionUrl == null || redirectionUrl.isEmpty()) {
            return seen;
        }

        return redirectionUrl.values().stream()   // Collection<List<String>>
                .flatMap(List::stream)            // Stream<String>
                .filter(url -> !seen.add(url))    // keep only those already seen
                .collect(Collectors.toSet());     // collect duplicates
    }
}
