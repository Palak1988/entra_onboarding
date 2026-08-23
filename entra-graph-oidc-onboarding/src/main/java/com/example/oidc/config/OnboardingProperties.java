package com.example.oidc.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.InitializingBean;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Configuration class to load onboarding properties
 */
@Configuration
@Slf4j
public class OnboardingProperties implements InitializingBean {

    // Uncomment if SSL trust store properties are needed
    // @Value("${ssl.trustStore}")
    // private String trustStore;
    // @Value("${ssl.trustStorePassword}")
    // private String trustStorePassword;
    // @Value("${ssl.trustStoreKeyEntry}")
    // private String trustStoreKeyEntry;

    @Resource
    private ConfigurationService configurationService;

    public static StaticProperties appAllStaticProperties;
    public static ServiceProperties appStaticProperties;
    public static Request requestMetadata;

    /**
     * This method is used to load properties
     * @throws Exception error
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        appAllStaticProperties = mapper.readValue(
                configurationService.findConfig("PROPERTIES", "onboardingProperties"),
                StaticProperties.class
        );
        requestMetadata = appAllStaticProperties.getRequestMetadata().get(0);
        setProxy();
    }

    /**
     * Set proxy system properties if enabled
     */
    public void setProxy() {
        if (appStaticProperties.isEnableProxy()) {
            System.setProperty(AppConstants.PROXY_HOST, appStaticProperties.getProxyHost());
            System.setProperty(AppConstants.PROXY_PORT, appStaticProperties.getProxyPort());
        }
    }

    /**
     * PostConstruct initialization logging SSL and trust store properties
     
    @PostConstruct
    public void init() {
        log.error("OnboardingProperties:PostConstruct init: START");
        log.error("OnboardingProperties:PostConstruct init: SSL_STORE_TYPE=" + System.getProperty("javax.net.ssl.trustStoreType"));
        log.error("OnboardingProperties:PostConstruct init: SSL_STORE=" + System.getProperty("javax.net.ssl.trustStore"));
        log.error("OnboardingProperties:PostConstruct init: SSL_STORE_PASSWORD=" + System.getProperty("javax.net.ssl.trustStorePassword"));
        log.error("OnboardingProperties:PostConstruct init: Property KEY_STORE_TYPE=" + System.getProperty("javax.net.ssl.trustStoreType"));
        log.error("OnboardingProperties:PostConstruct init: Property trustStore=" + System.getProperty("javax.net.ssl.trustStore"));
        log.error("OnboardingProperties:PostConstruct init: Property trustStorePassword=" + System.getProperty("javax.net.ssl.trustStorePassword"));
    } */
}
