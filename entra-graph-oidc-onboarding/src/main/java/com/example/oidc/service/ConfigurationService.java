package com.example.oidc.service;
import org.springframework.stereotype.Service;

import com.example.oidc.Exception.InvalidParameterException;
import com.example.oidc.model.Status;

import jakarta.annotation.Resource;

/** service to query configuration from table */
@Service
public class ConfigurationService {

    @Resource
    private RequestConfigRepository ConfigRepository;

    @Resource
    private RequestConfigurableService ConfigurableService;

    /**
     * query configuraion from configuration table 
     * find configuration by name
     * @param type config type
     * @param  Name config name
     * @return configuration in db
     */
    public RequestConfig findConfig(String type, String name) {
        return findConfigOptional(type, name)
                .orElseThrow(() -> new InvalidParameterException(String.format("There no active configuration found for type: %s and name: %s", type, name)));
    }
    
    /**
     * fetch configuraion from configuration table 
     * find configuration by name
     * @param type config type
     * @param  Name config name
     * @return configuration in db
     */

    public Optional<RequestConfig> findConfigOptional(String type, String name) {
        Status status = ConfigurableService.findStatus(Environment.ACTIVE.name());
        
        return ConfigRepository.findByTypeAndNameAndStatusid(type, name, status.getId());
}
