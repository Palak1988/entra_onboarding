package com.example.oidc.Dto;
import java.time.LocalDate;

import  com.example.oidc.model.Application;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor

public class OnboardingRequestResponseDto {
    private Long requestID;
    private String trackingId;
    private String status;
    private String message;
    private LocalDate createdDate;
    private LocalDate updatedDate;


    public OnboardingRequestResponseDto(Request request) {
       BeanUtils.copyProperties(request, this);
    }

    public OnboardingRequestResponseDto(Application application) {
        this.requestID = application.getId();
        this.trackingId = application.getTrackingId();
        this.status = application.getStatus();
        this.message = application.getMessage();
        this.createdDate = application.getCreatedDate();
        this.updatedDate = application.getUpdatedDate();
    }

    

    // Getters and setters
}