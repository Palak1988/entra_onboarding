package com.example.oidc.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.oidc.model.OidcApplication;
import com.example.oidc.service.GraphApiService;
import com.example.oidc.service.OnboardingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

public class OnboardingServiceTest {

    @InjectMocks
    private OnboardingService onboardingService;

    @Mock
    private GraphApiService graphApiService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOnboardApplication() {
        OidcApplication application = new OidcApplication();
        application.setClientId("test-client-id");
        application.setClientSecret("test-client-secret");
        application.setRedirectUris(Arrays.asList("http://localhost:8080/callback"));

        when(graphApiService.getAccessToken("test-client-id", "test-client-secret")).thenReturn("mock-access-token");

        OidcApplication result = onboardingService.onboardApplication(application);

        assertNotNull(result);
        assertEquals("test-client-id", result.getClientId());
        verify(graphApiService, times(0)).callGraphApi(anyString(), anyString());
    }

    @Test
    public void testValidateApplication() {
        String clientId = "test-client-id";

        OidcApplication application = onboardingService.validateApplication(clientId);

        assertNotNull(application);
        assertEquals(clientId, application.getClientId());
    }
}