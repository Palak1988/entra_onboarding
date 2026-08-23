package com.example.oidc.service.onboarding;
package com.sc.mfa.control.adoption.v1.service.onboarding.saml;

import com.jayway.jsonpath.DocumentContext;


/**
 * Service to onboard SAML in MFA for application
 */
@Service
public class SAMLOnboardingService extends OnboardingService {
    @Resource
    @Lazy
    OnboardingRequestService onboardingRequestService;

    public SAMLOnboardingService() {
        super(AuthCategory.SAML);
    }

    /**
     * create onboarding command based on request
     * @param request request from database
     * @return onboarding command for SAML
     */
    @Override
    protected OnboardingCommand createOnboardingCommand(OnboardingRequest request) {
        SAMLOnboardingCommand onboardingCommand = new SAMLOnboardingCommand();
        onboardingCommand.setRequestId(request.getTrackingId());
        Map<String, Object> metadata = request.getMetadata();
        onboardingCommand.setMetadata(metadata);
        DocumentContext context = JsonUtil.readContext(metadata);
        onboardingCommand.setContext(context);
        onboardingCommand.setApplicationName(APPLICATION_NAME.readVal(context));
        onboardingCommand.setApplicationId(request.getApplicationId());
        onboardingCommand.setTargetId(request.getTargetId());
        onboardingCommand.setTargetAppId(request.getTargetAppId());
        onboardingCommand.setTargetServicePrincipalId(request.getTargetServicePrincipalId());
        return onboardingCommand;
    }

    /**
     * execute onboarding steps
     * @param command onboarding command
     * @return onboarding result
     */
    @Override
    protected OnboardingCommand onboardApplication(OnboardingCommand command) throws Exception {
        onboardingRequestService.onboarding(command);
        return command;
    }
}