package com.example.oidc.Event;

public record OIDCOnboardingStepCompletedEvent(OIDCOnboardingPhase phase, Auditlog auditlog, OIDCOnboardingCommand onboardingCommand, Long applicationId) {
    
}
