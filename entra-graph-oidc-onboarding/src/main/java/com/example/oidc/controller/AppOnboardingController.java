package com.example.oidc.controller;

import com.example.oidc.model.OidcApplication;
import com.example.oidc.service.OnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oidc")
public class AppOnboardingController {

    private final OnboardingService onboardingService;

    @Autowired
    public AppOnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/applications")
    public ResponseEntity<OidcApplication> createApp(@RequestBody OidcApplication oidcApplication) {
        OidcApplication createdApp = onboardingService.onboardApplication(oidcApplication);
        return ResponseEntity.ok(createdApp);
    }

    @GetMapping("/applications/{clientId}")
    public ResponseEntity<OidcApplication> getApp(@PathVariable String clientId) {
        OidcApplication oidcApplication = onboardingService.validateApplication(clientId);
        return ResponseEntity.ok(oidcApplication);
    }
}