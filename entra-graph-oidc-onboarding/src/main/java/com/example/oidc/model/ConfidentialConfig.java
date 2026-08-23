package com.example.oidc.model;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.Set;

/**
 * Model class to hold confidential configuration
 */
@Getter
public class ConfidentialConfig {

    private Map<String, Set<String>> configuration;

    /**
     * Check if configuration exists for a given phase
     * @param phaseName step name of onboarding
     * @return true if config exists and has keys
     */
    public boolean containConfig(String phaseName) {
        return MapUtils.isNotEmpty(configuration)
                && configuration.containsKey(phaseName)
                && CollectionUtils.isNotEmpty(configuration.get(phaseName));
    }

    /**
     * Extract confidential keys for a given phase
     * @param phaseName step name of onboarding
     * @return set of confidential keys
     */
    public Set<String> extractConfidentialKeys(String phaseName) {
        return configuration.get(phaseName);
    }
}

