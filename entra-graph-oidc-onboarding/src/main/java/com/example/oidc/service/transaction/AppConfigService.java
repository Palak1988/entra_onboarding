package com.example.oidc.service.transaction;
package com.sc.mfa.control.adoption.v1.service.transaction;

import com.sc.mfa.control.adoption.configuration.OnboardingProperties;

/**
 * Service to save configuration for onboarding save configuration for open-id
 * table save configuration for saml table
 */
@Slf4j
@Service
public class AppConfigService {

    @Resource
    private AppOpenidConfigRepository appOpenidConfigRepository;

    @Resource
    private ConfigurableService configurableService;

    @Resource
    private AppSamlConfigRepository appSamlConfigRepository;

    @Resource
    private ApplicationRepository applicationRepository;

    @Resource
    private RequestRepository requestRepository;

    /**
     * save configuration
     *
     * @param applicationId application id in application table
     */
    public void saveOrUpdateOpenidConfig(Long applicationId) {
        Optional<Application> applicationOptional = applicationRepository.findById(applicationId);
        if (applicationOptional.isEmpty()) {
            return;
        }

        Application application = applicationOptional.get();
        Request request = requestRepository.findById(application.getRequestId()).orElseThrow(
                () -> new ResourceNotFoundException(String.format("cannot find request id %s by application id %s",
                        application.getRequestId(), applicationId)));

        if (request == null || request.getMetadata() == null) {
            return;
        }

        if (StringUtils.equals(AuthCategory.OIDC.name(), request.getType())) {
            appSamlConfigRepository.getByApplicationId(applicationId)
                    .ifPresent(app -> appSamlConfigRepository.delete(app));

            Optional<AppOpenidConfig> openidConfigOptional = appOpenidConfigRepository
                    .getByApplicationId(applicationId);
            AppOpenidConfig openidConfig = openidConfigOptional.orElseGet(AppOpenidConfig::new);
            openidConfig.setApplicationId(applicationId);

            openidConfig.setUserConsentRequired(Boolean.valueOf(JsonUtil
                    .getValueFromMap(request.getMetadata(),
                            OnboardingProperties.requestMetadata.getOidc().getUserConsentRequired())
                    .toString()));

            openidConfig.setAuthType((String) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getOidc().getAuthType()));

            openidConfig.setClaimAttributes((Map) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getOidc().getClaimAttributes()));

            openidConfig.setMtlsCert((String) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getOidc().getMtlsCert()));

            openidConfig.setCreatedDate(Objects.isNull(openidConfig.getCreatedDate()) ? SystemUtil.currentDateTime()
                    : openidConfig.getCreatedDate());
            openidConfig.setModifiedDate(SystemUtil.currentDateTime());

            openidConfig.setGrantType(String.valueOf(JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getOidc().getGrantType()).toString()));

            openidConfig.setRedirectionUrl((Map) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getOidc().getRedirectionUrl()));

            appOpenidConfigRepository.saveAndFlush(openidConfig);

        } else if (StringUtils.equals(AuthCategory.SAML.name(), request.getType())) {

            appOpenidConfigRepository.getByApplicationId(applicationId)
                    .ifPresent(oid -> appOpenidConfigRepository.delete(oid));

            AppSamlConfig samlConfig = appSamlConfigRepository.getByApplicationId(applicationId)
                    .orElseGet(AppSamlConfig::new);

            samlConfig.setApplicationId(applicationId);

            samlConfig.setAuthInitType((String) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getSaml().getAuthInitType()));

            samlConfig.setRequestSigning((String) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getSaml().getRequestSigning()));

            samlConfig.setResponseSigning((String) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getSaml().getResponseSigning()));

            samlConfig.setEncryptedSamlAssertions((String) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getSaml().getEncryptedSamlAssertions()));

            if (((String) JsonUtil.getValueFromMap(request.getMetadata(),
                    "appDefinition.appInstanceDefinition.authentication.samlAttributes.requestSigning")
                    .equals("SignedRequest"))) {

                samlConfig.setRequestSigningCert((String) JsonUtil.getValueFromMap(request.getMetadata(),
                        OnboardingProperties.requestMetadata.getSaml().getRequestSigningCert()));
            } else {
                samlConfig.setRequestSigningCert(null);
            }

            if (samlConfig.getEncryptedSamlAssertions().equals("Yes")) {
                samlConfig.setEncryptedSamlAssertionsCert((String) JsonUtil.getValueFromMap(request.getMetadata(),
                        OnboardingProperties.requestMetadata.getSaml().getEncryptedSamlAssertionsCert()));
            } else {
                samlConfig.setEncryptedSamlAssertionsCert(null);
            }

            samlConfig.setNameIdFormat((Map) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getSaml().getNameIdFormat()));

            samlConfig.setClaimAttributes((Map) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getSaml().getClaimAttributes()));

            samlConfig.setAssertionConsumerService(configurableService.getStringArrayFromObject(request,
                    OnboardingProperties.requestMetadata.getSaml().getAssertionConsumerService()));

            samlConfig.setEntityIdUri(configurableService.getStringArrayFromObject(request,
                    OnboardingProperties.requestMetadata.getSaml().getEntityIdUri()));

            samlConfig.setCreatedDate(Objects.isNull(samlConfig.getCreatedDate()) ? SystemUtil.currentDateTime()
                    : samlConfig.getCreatedDate());

            samlConfig.setModifiedDate(SystemUtil.currentDateTime());

            appSamlConfigRepository.saveAndFlush(samlConfig);
        }
    }
}