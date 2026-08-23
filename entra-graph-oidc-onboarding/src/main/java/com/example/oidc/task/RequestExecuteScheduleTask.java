package com.example.oidc.task;
package com.sc.mfa.control.adoption.v1.task;

import com.sc.mfa.control.adoption.v1.entity.Application;

/**
 * Scheduler to trigger onboarding request
 */
@Component
public class RequestExecuteScheduleTask {
    @Resource
    private OnboardingRequestService onboardingRequestService;
    @Resource
    private ApplicationDeleteService applicationDeleteService;
    @Resource
    private RequestRepository requestRepository;
    @Resource
    private ApplicationRepository applicationRepository;
    @Resource
    private ConfigurableService configurableService;

    /**
     * load pending request and trigger onboarding action
     */
    @Scheduled(cron = "${onboarding.request.execute:0/20 * * * * ?}")
    public void triggerRequest() {
        //Request
        List<Request> requests = requestRepository.findAllByStageIdAndStatusId(configurableService.findStage(RequestStage.APPROVED).getId(),
                configurableService.findStatus(RequestStatus.REQUESTED).getId());
        requests.forEach(e -> onboardingRequestService.triggerOnboarding(e.getTrackingId()));

        //Application delete
        List<Application> applications = applicationRepository.findAllByStageIdAndStatusId(
                configurableService.findStage(RequestStage.APPLICATION_DELETE_REQUEST).getId(),
                configurableService.findStatus(RequestStatus.REQUESTED).getId());
        applications.forEach(e -> {
            try {
                applicationDeleteService.applicationDelete(e.getTrackingId());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}