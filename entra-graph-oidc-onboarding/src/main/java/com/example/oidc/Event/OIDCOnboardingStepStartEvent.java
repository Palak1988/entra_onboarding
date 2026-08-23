package com.example.oidc.Event;

public record OIDCOnboardingStepStartEvent(OIDCOnboardingPhase phase, Auditlog auditlog, OIDCOnboardingCommand onboardingCommand, Long applicationId) {
    
}
