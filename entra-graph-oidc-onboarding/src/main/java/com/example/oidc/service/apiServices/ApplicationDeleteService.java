package com.example.oidc.service.apiServices;
import com.microsoft.graph.requests.GraphServiceClient;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Service to handle application deletion requests
 */
@Slf4j
@Service
public class ApplicationDeleteService {

    @Resource
    private ApplicationService applicationService;

    @Resource
    private RequestService requestService;

    @Resource
    private LockService lockService;

    @Resource
    private ConfigurableService configurableService;

    @Resource
    private GraphServiceClient graphServiceClient;

    /**
     * Handle application delete request
     * @param trackingId request tracking identifier
     * @return onboarding command result
     */
    public OnboardingCommand applicationDeleteRequest(String trackingId) {
        log.info("Received application delete request for trackingId [{}]", trackingId);

        // Fetch request details
        Request request = requestService.findByTrackingId(trackingId);
        if (request == null) {
            log.error("No request found for trackingId [{}]", trackingId);
            throw new APIException(APIErrorCode.NOT_FOUND, "Request not found");
        }

        // Lock application for deletion
        lockService.lock(request.getApplicationId());

        try {
            return applicationDelete(trackingId);
        } finally {
            lockService.unlock(request.getApplicationId());
        }
    }

    /**
     * Perform application deletion
     * @param trackingId request tracking identifier
     * @return onboarding command result
     */
    public OnboardingCommand applicationDelete(String trackingId) {
        log.info("Deleting application for trackingId [{}]", trackingId);

        Request request = requestService.findByTrackingId(trackingId);
        if (request == null) {
            log.error("No request found for trackingId [{}]", trackingId);
            throw new APIException(APIErrorCode.NOT_FOUND, "Request not found");
        }

        // Update request status
        requestService.updateStatus(request.getId(), RequestStatus.DELETING);

        // Delete application from system
        applicationService.deleteApplication(request.getApplicationId());

        // Delete application from Microsoft Graph
        graphServiceClient.deleteApplication(request.getApplicationId());

        // Update request status to completed
        requestService.updateStatus(request.getId(), RequestStatus.DELETED);

        log.info("Application deletion completed for trackingId [{}]", trackingId);

        return new OnboardingCommand(request.getApplicationId(), RequestStatus.DELETED);
    }
}
