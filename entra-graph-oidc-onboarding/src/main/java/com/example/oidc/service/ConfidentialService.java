package com.example.oidc.service;

import com.example.oidc.model.ConfidentialConfig;

import jakarta.annotation.Resource;

/**
 * Service to remove sensitive data in the map
 */
@Service
public class ConfidentialService {

    
    @Resource
    private ConfigurationService configurationService;

    /**
     * remove sensitive data in the input
     * @param phaseName step name of onboarding
     * @param input object which need to save to db
     * @return result
     */
    public Object removeSensitive(String phaseName,
                                  Object input) {

        if (Objects.isNull(input)) {
            return null;
        }

        if (input instanceof Map<?, ?> data) {
            return removeSensitiveMap(phaseName, (Map<String, Object>) data);
        }

        return input;
    }

    /**
     * remove sensitive data in the input
     * @param phaseName step name of onboarding
     * @param input map which may contain sensitive information
     * @return result
     */
    public Map<String, Object> removeSensitiveMap(String phaseName,
                                                  Map<String, Object> input) {

        if (MapUtils.isEmpty(input)) {
            return Collections.emptyMap();
        }

        RequestConfig config = configurationService.findConfigOptional("SYSTEM",
                "ConfidentialConfig").orElseGet(() -> new RequestConfig());

        Optional<ConfidentialConfig> configOptional =
                JsonUtil.parse(config.getConfigData(),
                        ConfidentialConfig.class);

        if (configOptional.isEmpty() || !configOptional.get().containConfig(phaseName)) {
            return SystemUtil.clone(input);
        }

        ConfidentialConfig confidentialConfig = configOptional.get();

        Map<String, Object> result = SystemUtil.clone(input);

        removeSensitive(result,
                confidentialConfig.extractConfidentialKeys(phaseName));

        return result;
    }

    private Map<String, Object> removeSensitive(Map<String, Object> data,
                                                Set<String> confidentialKeys) {

        for (Map.Entry<String, Object> entry : data.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();

            if (confidentialKeys.contains(key)) {
                data.put(key, removeAll(value));
                continue;
            }

        if (value instanceof Map<?, ?> data) {
             removeSensitive((Map<String, Object>) data, confidentialKeys);
            } else if (value instanceof List<?> array) {
    verify(array, confidentialKeys);
}
        }

return data;
}

private void verify(List<?> array, Set<String> confidentialKeys) {
    for (Object o : array) {
        verifyObject(o, confidentialKeys);
    }
}

private void verifyObject(Object value, Set<String> confidentialKeys) {
    if (value instanceof Map<?, ?> data) {
        removeSensitive((Map<String, Object>) data, confidentialKeys);
    }
}

private Object removeAll(Object value) {

    if (value instanceof String) {
        return "**";
    }

    if (value instanceof Integer || value instanceof Long) {
        return 0;
    }

    if (value instanceof Map<?, ?> data) {
        Map<String, Object> remove = (Map<String, Object>) data;

        for (Map.Entry<String, Object> entry : remove.entrySet()) {
            String key = entry.getKey();
            Object tmp = entry.getValue();
            remove.put(key, removeAll(tmp));
        }
    }

    if (value instanceof List<?> array) {
        return removeAllSensitive(array);
    }

    return value;
}

private List<?> removeAllSensitive(List<?> array) {

    if (CollectionUtils.isEmpty(array)) {
        return Collections.emptyList();
    }

    List<Object> result = new ArrayList<>(array.size());

    for (Object value : array) {
        result.add(removeAll(value));
    }

    return result;
}
}