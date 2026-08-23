package com.example.oidc.Event;

public record SAMLOnboardingStepStartEvent(SAMLOnboardingPhase phase, Auditlog auditlog, SAMLOnboardingCommand onboardingCommand, Long applicationId) {
    
}
