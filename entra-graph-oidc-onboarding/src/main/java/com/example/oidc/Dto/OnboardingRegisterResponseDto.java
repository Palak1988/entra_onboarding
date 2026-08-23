package com.example.oidc.Dto;

public record OnboardingRegisterResponseDto(
    Long requestID, String trackingId, String status, String message) {
}
