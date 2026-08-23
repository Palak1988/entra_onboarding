package com.example.oidc.Listener;
package com.sc.mfa.control.adoption.v1.listener;


import com.sc.mfa.control.adoption.v1.listener.event.OIDCOnboardingStepCompletedEvent;

/**
 * OIDC step event listener
 */
@Component
@Slf4j
public class OIDCStepEventListener {

    @Resource
    ApplicationService applicationService;

    /**
     * handle OIDC onboard step completed event to update application stage and status
     * @param event event object
     */
    @EventListener
    public void deal(OIDCOnboardingStepCompletedEvent event) {
        applicationService.updateStageOrStatus(event.onboardingCommand().getApplicationId(), RequestStage.COMPLETED.name(), RequestStatus.SUCCESS);
    }

    /**
     * handle OIDC onboard step start event to update application stage and status
     * @param event event object
     */
    @EventListener
    public void deal(OIDCOnboardingStepStartEvent event) {
        applicationService.updateStageOrStatus(event.onboardingCommand().getApplicationId(), event.phase().toString(), RequestStatus.IN_PROGRESS);
    }

    /**
     * handle OIDC onboard step failed event to update application stage and status
     * @param event event object
     */
    @EventListener
    public void deal(OIDCOnboardingStepFailureEvent event) {
        applicationService.updateStageOrStatus(event.onboardingCommand().getApplicationId(), null, RequestStatus.FAILURE);
    }
}