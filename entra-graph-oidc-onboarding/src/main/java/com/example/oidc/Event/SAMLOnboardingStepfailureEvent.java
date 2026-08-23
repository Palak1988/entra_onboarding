package com.example.oidc.Event;
import lombook.AllArgsConstructor;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@lombok.AllArgsConstructor

public record SAMLOnboardingStepfailureEvent(SAMLOnboardingPhase phase, Auditlog auditlog, SAMLOnboardingCommand onboardingCommand, Long applicationId) {

    private Long applicationId;

    
    
}
