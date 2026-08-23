package com.example.oidc.service;


import com.example.oidc.config.OnboardingProperties;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

/**
 * Service to merge metadata for onboarding
 */
public class DefaultValueService {

    @Resource
    private ConfigurationService configurationService;

    @Resource
    private ConfigurableService configurableService;

    /**
     * merge default value with application's manifest
     *
     * @param env environment of onboarding
     * @param parameters parameters from manifest
     * @param category category of onboarding
     * @return manifest with full values
     */
    public Map<String, Object> fillDefaultValues(String env,
                                                 Map<String, Object> parameters,
                                                 AuthCategory category) {

        // Remove default auth groups from manifest
        removeKeyValueFromMap(parameters,
                OnboardingProperties.requestMetadata.getCommon().getDefaultAuthGroups());

        // Access group definitions for SAML and OIDC
        OnboardingProperties.requestMetadata.getSaml().getGroups();
        OnboardingProperties.requestMetadata.getOidc().getGroups();

        // Load default configuration based on environment
        String configName = "DefaultConfiguration" + env.toUpperCase();
        RequestConfig config = configurationService.findConfig(category.name(), configName);

        Map<String, Object> defaultValues = JsonUtil.parse(config.getConfigData(), Map.class)
                .orElseGet(Collections::emptyMap);

        if (MapUtils.isEmpty(defaultValues)) {
            return SystemUtil.clone(parameters);
        }

        Map<String, Object> metadata = defaultValues;
        if (MapUtils.isEmpty(parameters)) {
            return metadata;
        }

        // Merge default values with manifest parameters
        Map<String, Object> copy = SystemUtil.clone(parameters);
        SystemUtil.merge(metadata, copy, category.getOverrideIndex());

        return metadata;
    }
}

