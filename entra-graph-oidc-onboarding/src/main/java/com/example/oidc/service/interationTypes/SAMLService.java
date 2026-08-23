package com.example.oidc.service.interationTypes;

package com.sc.mfa.control.adoption.v1.service.integrationTypes;

import java.util.Arrays;

@Slf4j
@Service
public class SamlService extends GraphService {
    @Autowired
    CommonUtil commonUtil;
    @Autowired
    SamlUtil samlUtil;

    /**
     * This method is used to onboard saml application
     * @param command payload metadata
     * @param commonService object
     * @param graphServiceClient graph service connection
     * @throws Exception handling
     */
    public void onboardApplication(OnboardingCommand command, CommonService commonService,
                                   GraphServiceClient graphServiceClient) throws Exception {
        boolean isnew = false;
        try {
            this.graphServiceClient = graphServiceClient;
            Application application = new Application();
            // load application if already exists
            if (Objects.isNull(command.getResult().get(OnboardingResultKey.OBJECT_ID.getKey()))) {
                command = createApplication(command);
                isnew = true;
            } else {
  //              application = graphServiceClient.applications()
   //                 .withAppId(command.getResult().get(OnboardingResultKey.APP_ID.getKey())).toObject();
                resetCert(application);
            }

            ServicePrincipal servicePrincipal = new ServicePrincipal();
            servicePrincipal.setPreferredSingleSignOnMode(AuthType.SAML.name().toLowerCase());
            servicePrincipal.setNotificationEmailAddresses(Arrays.asList("Auth-SvcsAppOn-Boarding@sc.com"));
            servicePrincipal.setNotificationEmailAddresses(command.getNotificationMails());
            //Response signing certificate
            samlUtil.responseSigningCertificate(servicePrincipal, command.getSamlSigningPFX(),
                command.getSamlSigningPublic(), String.valueOf(command.getSamlSigningKeyPass()));

            // Service Principal call
            servicePrincipalPatch(servicePrincipal,
                String.valueOf(command.getResult().get(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey())));

            // Update application URL details
            // Update application url details
commonUtil.optionalClaims(application, AuthCategory.SAML.name(), command.getEnv());
application.setIdentifierUris(Arrays
    .stream(commonUtil.getStringArrayFromObject(JsonUtil.getValueFromMap(command.getMetadata(),
        OnboardingProperties.requestMetadata.getSaml().getEntityIdUri())))
    .collect(Collectors.toList()));

WebApplication web = new WebApplication();
web.setRedirectUris(Arrays
    .stream(commonUtil.getStringArrayFromObject(JsonUtil.getValueFromMap(command.getMetadata(),
        OnboardingProperties.requestMetadata.getSaml().getAssertionConsumerService())))
    .collect(Collectors.toList()));
application.setWeb(web);

// Request signing and encrypt
application = samlUtil.setRequestSigningEncryptCertificate(application,
    String.valueOf(JsonUtil.getValueFromMap(command.getMetadata(),
        OnboardingProperties.requestMetadata.getSaml().getRequestSigningCert())),
    String.valueOf(JsonUtil.getValueFromMap(command.getMetadata(),
        OnboardingProperties.requestMetadata.getSaml().getEncryptedSamlAssertionsCert())));

String dn = commonUtil.getMetadataValue(command,
    OnboardingProperties.requestMetadata.getCommon().getDisplayName());
application.setDisplayName(dn);

postOrPatchApplication(false, application,
    String.valueOf(command.getResult().get(OnboardingResultKey.OBJECT_ID.getKey())));

log.info("SAML application onboarding completed successfully");
} catch (Exception e) {
    if (isnew) {
        commonService.deleteApplication(command);
    }
    OnboardingCommand finalCommand = command;
    command.getResult().entrySet().removeIf(entry -> finalCommand.getResult().containsKey(entry.getKey()));
    log.error("ERROR = " + e.getMessage());
    e.printStackTrace();
    throw e;
}

private OnboardingCommand createApplication(OnboardingCommand command) {
    InstantiatePostRequestBody instantiatePostRequestBody = new InstantiatePostRequestBody();
    instantiatePostRequestBody.setDisplayName(commonUtil.getMetadataValue(command,
        OnboardingProperties.requestMetadata.getCommon().getDisplayName()));

        // GS 1 ::: Get applicationTemplateID
String applicationTemplateID = getApplicationTemplateID(
    AppFilterRules.getApplicationTemplatesRequestBuilder("Custom"));

// GS 2 ::: Create Application template
ApplicationServicePrincipal appTemplate = getApplicationServicePrincipal(instantiatePostRequestBody,
    applicationTemplateID);
command.getResult().put(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey(),
    Objects.requireNonNull(appTemplate.getServicePrincipal()).getId());
commonUtil.setEntraAppIds(command, Objects.requireNonNull(appTemplate.getApplication()).getId(),
    appTemplate.getApplication().getAppId());
commonUtil.getAppRoleId(Objects.requireNonNull(appTemplate.getApplication().getAppRoles())));

return command;
}

    public void resetCert(Application application) {
        application.setTokenEncryptionKeyId(null);
        RequestSignatureVerification reqsig = new RequestSignatureVerification();
        reqsig.setIsSignedRequestRequired(false);
        reqsig.setAllowedWeakAlgorithms(null);
        application.setRequestSignatureVerification(reqsig);
    }

}
}
}
