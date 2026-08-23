package com.example.oidc.service.onboarding;
package com.sc.mfa.control.adoption.v1.service.onboarding.oidc;

import com.jayway.jsonpath.DocumentContext;


/**
 * Service to onboard OIDC in MFA for application
 */
@Service
@Slf4j
public class OIDCOnboardingService extends OnboardingService {
    @Resource
    @Lazy
    OnboardingRequestService onboardingRequestService;

    public OIDCOnboardingService() {
        super(AuthCategory.OIDC);
    }

    /**
     * create onboarding command based on request
     *
     * @param request request from database
     * @return onboarding command for OIDC
     */
    @Override
    protected OnboardingCommand createOnboardingCommand(OnboardingRequest request) {
        OIDCOnboardingCommand onboardingCommand = new OIDCOnboardingCommand();
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
        onboardingCommand.setMicrosoftGraphClientEntry("".toCharArray());
        return onboardingCommand;
    }

    /**
     * execute onboarding steps
     *
     * @param command onboarding command
     * @return onboarding result
     */
    @Override
    protected OnboardingCommand onboardApplication(OnboardingCommand command) throws Exception {
        onboardingRequestService.onboarding(command);
        return command;
    }
}