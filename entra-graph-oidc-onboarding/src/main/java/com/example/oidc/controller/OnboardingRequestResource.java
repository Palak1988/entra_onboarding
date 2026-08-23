package com.example.oidc.controller;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/api/v1/entra/application/onboarding")
@RestController

public class OnboardingRequestResource {

@Resource
private OnboardingRequestService onboardingRequestService;

@Resource
private ApplicationDeleteService applicationDeleteRequest;

@Resource
private UpdateStatusService updateStatusService;
@Resource
AESHelper aesHelper;

@PostMapping("requests")
public SuccessResponse<OnboardingRegisterResponseDto> registerOnboardingRequest
(@Validated @RequestBody OnboardingRegisterRequestDto requestDto) throws Exception {
requestDto.setTrackingId(UUID.randomUUID().toString());
OnboardingRegisterResponseDto result = onboardingRequestService.register(requestDto);
return new SuccessResponse<>(result);
}

@PatchMapping("requests/{trackingId}")
public SuccessResponse<OnboardingRegisterResponseDto> updateOnboardingRequest(@PathVariable String trackingId, @Validated @RequestBody OnboardingRegisterRequestDto requestDto) throws Exception {
requestDto.setTrackingId(trackingId);
OnboardingRegisterResponseDto result = onboardingRequestService.register(requestDto);
return new SuccessResponse<>(result);
}

@DeleteMapping("delete/{trackingId}")
public OnboardingRegisterResponseDto deleteApplication(@PathVariable String trackingId) {
return applicationDeleteRequest.applicationDeleteRequest(trackingId);
}

@PatchMapping("stage/{trackingId}")
public OnboardingRegisterResponseDto updateStage(@PathVariable String trackingId, @RequestParam(AppConstants.STAGE) String stage, @RequestBody(required = false) Map<String, Object> comments) {
return updateStatusService.updateStage(trackingId, stage, comments);
}

@GetMapping("requests:find-by-tracking-id")
public SuccessResponse<OnboardingRequestResponseDto> queryRequest(@RequestParam String trackingId) {
return new SuccessResponse<>(onboardingRequestService.findRequest(trackingId));
}

@GetMapping("aes")
public String key(@RequestBody String keyPass, @RequestParam(AppConstants. TYPE) String type) throws Exception {
return type.equals(AppConstants.DECRYPT) ? aesHelper.decrypt(keyPass) : aesHelper.encrypt(keyPass);
}