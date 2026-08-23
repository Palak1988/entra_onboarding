package com.example.oidc.Listener;
package com.sc.mfa.control.adoption.v1.listener;


import com.sc.mfa.control.adoption.v1.listener.event.*;

 /**
  * SAML onboard step event listener
  */
@Component
@Slf4j
public class SAMLStepEventListener {

    @Resource
    ApplicationService applicationService;

    /**
     * handle step completed event to update application stage and status
     * @param event event object
     */
    @EventListener
    public void deal(SAMLOnboardingStepCompletedEvent event) {
        applicationService.updateStageOrStatus(event.onboardingCommand().getApplicationId(), RequestStage.COMPLETED.name(), RequestStatus.SUCCESS);
    }

    /**
     * handle step start event to update application stage and status
     * @param event event object
     */
    @EventListener
    public void deal(SAMLOnboardingStepStartEvent event) {
        applicationService.updateStageOrStatus(event.onboardingCommand().getApplicationId(), event.phase().toString(), RequestStatus.IN_PROGRESS);
    }

    /**
     * handle step failed event to update application stage and status
     * @param event event object
     */
    @EventListener
    public void deal(SAMLOnboardingStepFailureEvent event) {
        applicationService.updateStageOrStatus(event.onboardingCommand().getApplicationId(), null, RequestStatus.FAILURE);
    }
}