package com.example.oidc.service.apiServices;
import com.example.oidc.model.OnboardingRegisterStatus;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Map;

@Service
@Slf4j
public class UpdateStatusService {

    @Resource
    private OnboardingRequestService requestService;

    @Resource
    private ApplicationService applicationService;

    public OnboardingRegisterResponseDto updateStage(String trackingId, String stage, Map<String, Object> comments) {
        Application application = applicationService.updateStageOrStatus(
                applicationService.findApplication(trackingId).getId(),
                stage,
                null,
                comments);
        

        return new OnboardingRegisterResponseDto(
                requestService.updateStageOrStatus(
                        trackingId,
                        application.getStageId(),
                        application.getStatusId()).getId(),
                        trackingId,
                        STAGE_SUCCESSFULLY.getMessage()
                );
    
    }
}
