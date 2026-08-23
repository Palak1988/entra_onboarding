package com.example.oidc.Event;

public record OIDCOnboardingStepfailureEvent(OIDCOnboardingPhase phase, Auditlog auditlog, OIDCOnboardingCommand onboardingCommand, Long applicationId) {
    
}
