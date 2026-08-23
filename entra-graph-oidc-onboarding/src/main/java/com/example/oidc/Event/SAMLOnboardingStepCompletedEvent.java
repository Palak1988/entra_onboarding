package com.example.oidc.Event;

public record SAMLOnboardingStepCompletedEvent(SAMLOnboardingPhase phase, Auditlog auditlog, SAMLOnboardingCommand onboardingCommand, Long applicationId) {
    
}
