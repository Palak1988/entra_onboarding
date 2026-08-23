package com.example.oidc.service.apiServices;
import static com.example.oidc.model.OnboardingMetadataProperty.ITAM_ID;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to manage onboarding request
 */
@Service
@Slf4j
public class OnboardingRequestService {
    private final Map<AuthCategory, OnboardingService> onboardingServiceMap;

    @Autowired
    DatabaseTemplateParser databasetemplateParser;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    ConfigurableService configurableService;
    @Autowired
    LockService lockService;
    @Autowired
    CommonUtil commonUtil;
    @Autowired
    OIDCService oidcService;
    @Autowired
    SamlService samlService;
    @Autowired
    HashicorpService hashicorpService;
    @Autowired
    DefaultValueService defaultValueService;
    @Autowired
    ConfigurationService configurationService;
    @Autowired
    ValidationService validationService;
    @Autowired
    ApplicationRepository applicationRepository;
    @Autowired
    OKHttpClient okHttpClient;

    @Autowired
    ServiceNowCrValidatorUtil serviceNowCrValidatorUtil;

    public OnboardingRequestService(List<OnboardingService> onboardingServices) {
        onboardingServiceMap = onboardingServices.stream()
                .collect(Collectors.toMap(OnboardingService::getCategory, e -> e));
    }
    /* *
    private Request refreshRequest(Request old, Request newRequest) { //could not update request
        if (newRequest.getStatus() != ProgressStatus.IN_PROGRESS_STATUS) {
            throw new IllegalStateException("Could not update request");
        }
        old.setParameters(newRequest.getParameters());
        old.setTrackingId(newRequest.getTrackingId());
        old.setNextFieldDueDate(newRequest.getNextFieldDueDate());
        old.setExecutedTimes(0);
        return old;
    }
    */
    // trigger the onboarding process
}
    
    /**
     * trigger the onboarding process
     * @param trackingId trackId from SCM
     */

    public void triggerOnboarding(String trackingId) {
        // fetch execute lock
        if (!lockService.lock(trackingId)) {
            log.info("failed to fetch lock for request [{}], skip to execute", trackingId);
            return;
        }

        // query the request with id
        Request request = requestRepository.findByTrackingId(trackingId)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("request %s is not found", trackingId)));

        try {
            // verify request latest status
            if (RequestStatus.isCompleted(
                    configurableService.findStatusEnum(request.getStatusId()))) {
                log.info("request [{}] has been completed, skip to execute", request.getTrackingId());
                return;
            }

            increaseExecuteTime(request);

            request.setUpdatedTime(SystemUtil.currentDateTime());
            requestRepository.saveAndFlush(request);

            AuthCategory category = AuthCategory.valueOf(request.getType());
            Application app = applicationService.findByRequestId(request.getId());

            OnboardingRequest onboardingRequest = convertRequest(request);
            onboardingRequest.setApplicationId(app.getId());
            onboardingRequest.setTargetId(app.getTargetId());
            onboardingRequest.setTargetServicePrincipalId(app.getTargetServicePrincipalId());
            onboardingCommandService.principal(app.getInitiateServicePrincipalId());
            applicationService.onboard(app.getId(), command);

            Status successStatus = configurableService.findStatus(RequestStatus.SUCCESS.getId());
            Stage stage = configurableService.findStage(RequestStage.COMPLETED.getId());
            request.setStageId(stage.getId());
            request.setStatusId(successStatus.getId());
            requestRepository.saveAndFlush(request);

            List<Request> completedReq = requestRepository.findByStatus(successStatus.getId());
            completedReq.forEach(r -> {
                r.setStatusId(configurableService.findStatus(
                        RequestStatus.APPLICATION_DELETE_REQUEST.getId()));
                r.setStatusId(configurableService.findStatus(RequestStatus.SUCCESS.getId()));
                requestRepository.saveAndFlush(r);
            });
        } catch (Exception e) {
            e.printStackTrace();
            Request retryRequest = requestRepository.findByTrackingId(trackingId).get();
            applicationService.retryOption(e, trackingId, retryRequest,
                    configurableService, requestRepository);
        } finally {
            lockService.unlock(trackingId);
        }
    }
    /**
     * Register onboarding request
     * @param requestDto onboarding registration request
     * @return onboarding registration response
     * @throws Exception if registration fails
     */
    public OnboardingRegisterResponseDto register(OnboardingRegisterRequestDTO requestDto) throws Exception {
        // Verify if request already exists
        Optional<Request> existingRequest = requestRepository.findByTrackingId(requestDto.getTrackingId());
        if (existingRequest.isPresent()) {
            Request completedRequest = existingRequest.get();
            if (RequestStatus.isCompleted(configurableService.findStatusEnum(completedRequest.getStatusId()))) {
                log.info("Request [{}] already completed, skipping registration", requestDto.getTrackingId());
                return new OnboardingRegisterResponseDto(completedRequest.getId(),
                        requestDto.getTrackingId(), "Already Completed");
            }
            if (RequestStatus.isHold(configurableService.findStatusEnum(completedRequest.getStatusId()))) {
                log.info("Request [{}] is on hold, skipping registration", requestDto.getTrackingId());
                return new OnboardingRegisterResponseDto(completedRequest.getId(),
                        requestDto.getTrackingId(), "On Hold");
            }
            if (RequestStatus.isCancelled(configurableService.findStatusEnum(completedRequest.getStatusId()))) {
                log.info("Request [{}] is cancelled, skipping registration", requestDto.getTrackingId());
                return new OnboardingRegisterResponseDto(completedRequest.getId(),
                        requestDto.getTrackingId(), "Cancelled");
            }
            if (RequestStatus.isDeleteRequest(configurableService.findStatusEnum(completedRequest.getStatusId()))) {
                log.info("Request [{}] is marked for deletion, skipping registration", requestDto.getTrackingId());
                return new OnboardingRegisterResponseDto(completedRequest.getId(),
                        requestDto.getTrackingId(), "Marked for Deletion");
            }
        }

        // Create new request
        Request request = new Request();
        request.setTrackingId(requestDto.getTrackingId());
        request.setParameters(requestDto.getParameters());
        request.setCreatedDate(SystemUtil.currentDateTime());
        request.setStatusId(configurableService.findStatus(RequestStatus.IN_PROGRESS).getId());
        request.setStageId(configurableService.findStage(RequestStage.REGISTERED).getId());

        // Save request
        requestRepository.saveAndFlush(request);

        log.info("Registered new onboarding request [{}]", requestDto.getTrackingId());
        return new OnboardingRegisterResponseDto(request.getId(),
                requestDto.getTrackingId(), "Registered Successfully");
    }
    if (AppConstants.PROD_TENANT.equals(tenant)) {
        validateState(createField, enumMap, instanceMap, false);
    }
    AuthCategory category = requestDto.extractAuthCategory();
    request.setStatus(configurableService.getStatus(requestDto));
    request.setStage(configurableService.findStage(requestDto.get(AppConstants.STAGE).getId()));
    request.setMetadata(configurableService.fillDefaultValues(commonUtil.getItem(requestDto.getAppParameters())));
    request.setUpdatedDate(SystemUtil.currentDateTime());

    requestValidationService.validate(request);
    if (CollectionUtils.isNotEmpty(request.getErrors())) {
        log.info("request ID doesn't pass the validation. error={}", request.getTrackingId(), error);
        throw new InvalidArgumentException(error);
    }

    requestRepository.saveAndFlush(request);
    requestRepository.saveAndFlush(completedReq);

    Application app = applicationService.process(request, category, completedReq);
    applicationContext.publishEvent(new RequestProcessEvent(request.getId(), request.getTrackingId(), category));

    if (requestDto.get(AppConstants.STAGE).equals(RequestStage.SUBMITTED_FOR_APPROVAL.toString())) {
        try {
            validateRequest(request, app, category);
        } catch (Exception e) {
            request.setStage(configurableService.findStage(RequestStage.FAILED).getId());
            request.setStatus(configurableService.failureStatus().getId());
            request.setModifiedDate(SystemUtil.currentDateTime());
            requestRepository.saveAndFlush(request);

            app.setStatus(configurableService.findStatus(RequestStatus.FAILED).getId());
            app.setErrorMessage(e.getMessage());

            log.error("error message while validate request calling: ", e);
            throw new InvalidArgumentException(e.getMessage());
        }
    }

    // register request to database
    return new OnboardingRequestResponseDto(request.getId(), requestDto.getTrackingId(),
            "REGISTERED SUCCESSFULLY");
}

/**
 * Increase execution count for request
 */
private void increaseExecuteTime(Request request) {
    request.setExecutedItems(request.getExecutedItems() + 1);
    requestRepository.saveAndFlush(request);
}

/**
 * Find onboarding request by trackingId
 */
public OnboardingRequestResponseDto findRequest(String trackingId) {
    Optional<Request> requestOpt = requestRepository.findByTrackingId(trackingId);
    if (requestOpt.isPresent()) {
        Request request = requestOpt.get();
        Application application = applicationService.findApplication(trackingId);
        return createOnboardingResponseDto(application);
    }
    return null;
}

/**
 * Convert Request entity to OnboardingRequest DTO
 */
private OnboardingRequest convertRequest(Request request) {
    OnboardingRequest onboardingRequest = new OnboardingRequest();
    BeanUtils.copyProperties(request, onboardingRequest);
    onboardingRequest.setTrackingId(request.getTrackingId());
    onboardingRequest.setCategory(AuthCategory.valueOf(request.getType()));
    Map<String, Object> clone = SystemUtil.clone(request.getMetadata());
    onboardingRequest.setMetadata(clone);
    return onboardingRequest;
}
/**
 * Create onboarding request response DTO from Request
 */
private OnboardingRequestResponseDto createOnboardingRequestResponseDto(Request request) {
    Status status = configurableService.findStatus(request.getStatusId());
    return new OnboardingRequestResponseDto(request.getId(),
            request.getTrackingId(), status.getName());
}

/**
 * Create onboarding request response DTO from Application
 */
private OnboardingRequestResponseDto createOnboardingRequestResponseDto(Application application) {
    Status status = configurableService.findStatus(application.getStatusId());
    return new OnboardingRequestResponseDto(application.getId(),
            application.getTrackingId(), status.getName());
}

/**
 * Handle onboarding process
 */
public OnboardingCommand onboarding(OnboardingCommand command) throws Exception {
    CommonService commonService = null;
    ValidationCommand validationCommand = null;
    ConfigurableEnvironment environment = configurableService.getEnvironment();

    try {
        // Setup environment values
        command.setEnv(environment.getEnvName());
        log.info("onboarding : INFO starting onboarding for [{}]", command.getTrackingId());

        // Perform validation
        validateOnboarding(command);

        // Execute onboarding logic
        commonService = new CommonService();
        validationCommand = new ValidationCommand(command);
        validationCommand.execute();

        log.info("onboarding : INFO completed successfully for [{}]", command.getTrackingId());
    } catch (Exception e) {
        log.error("onboarding : ERROR during onboarding for [{}]", command.getTrackingId(), e);
        throw e;
    }
    return command;
}

/**
 * Validate onboarding command
 */
private void validateOnboarding(OnboardingCommand command) {
    try {
        Map<String, Object> metadata = command.getMetadata();
        String tenant = (String) metadata.get("tenant");
        String instanceName = (String) metadata.get("instanceName");
        String cfFieldId = (String) metadata.get("cfFieldId");

        if (AppConstants.PROD_TENANT.equals(tenant)) {
            validateState(cfFieldId, tenant, instanceName, true);
        }
    } catch (InvalidParameterException e) {
        log.error("validateOnboarding : ERROR invalid parameters for [{}]", command.getTrackingId(), e);
        throw e;
    }
}
/**
 * Perform onboarding with integration type
 * @param command onboarding command
 * @param graphServiceClient Microsoft Graph client
 * @param commonService common service
 * @param getIntegrationType integration type map
 * @throws Exception if onboarding fails
 */
public OnboardingCommand Onboard(OnboardingCommand command,
        GraphServiceClient graphServiceClient,
        CommonService commonService,
        HashMap<String, Object> getIntegrationType) throws Exception {

    // Internal Mandatory - Application display name
    command = commonService.getDisplayNameSetInAppIds(command);

    if (getIntegrationType.containsKey(
            com.sc.mfa.control.adoption.common.AuthCategory.OIDC.getStc())) {
        // OIDC onboarding
        oidcService.onboardApplication(command, graphServiceClient);
        // Claims mapping policy
        commonService.CreateUpdateClaimsMappingPolicy(command,
                com.sc.mfa.control.adoption.common.AuthCategory.OIDC.name().toLowerCase(),
                databaseTemplateParser);
    } else {
        // SAML onboarding
        samlService.onboardApplication(command, commonService, graphServiceClient);
        // Claims mapping policy
        commonService.CreateUpdateClaimsMappingPolicy(command,
                com.sc.mfa.control.adoption.common.AuthCategory.SAML.name().toLowerCase(),
                databaseTemplateParser);
    }

    // End Onboarding
    if ((boolean) command.getResult().get("claimsMappingPoliciesExist")) {
        commonService.assignClaimsMappingPolicy(command);
    }

    // Assign group
    commonService.assignGroupForApp(command,
            command.getResult().get(
                    com.sc.mfa.control.adoption.common.OnboardingResultKey.APP_ROLL_ID.getKey())
                    .toString());

    // Custom security attribute
    commonService.customSecurityAttribute(command);

    log.info("Application onboarding completed successfully");
    return command;
}

/**
 * Update stage or status for a request
 */
public Request updateStageOrStatus(String trackingId, long stageId, long statusId) {
    Request request = requestRepository.findByTrackingId(trackingId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("request %s is not found", trackingId)));

    request.setStageId(stageId);
    request.setStatusId(statusId);
    request.setModifiedTime(LocalDateTime.now());

    if (request.getStatusId().equals(S2L)) {
        request.setModifiedTime(LocalDateTime.now().plusHours(1));
    }

    if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
        request.getMetadata().stream()
                .filter(req -> req.getMetadata().equals(
                        OnboardingProperties.REQUEST_METADATA.getInstanceId()))
                .collect(Collectors.toList())
                .forEach(a -> {
                    a.setStatusId(configurableService.findStatus(RequestStatus.SUCCESS).getId());
                });
        requestRepository.saveAndFlush(request);
    }
    return request;
}
    requestRepository.saveAndFlush(request);
    return request;
}

/**
 * Validate state for onboarding
 */
private void validateState(String crefId, String envName, String instanceName, boolean isOnboarding) {
    if (crefId != null) {
        serviceCheckValidatorUtil.validator(crefId, envName, instanceName, isOnboarding);
    }
}

/**
 * Validate request against environment and Microsoft Graph
 */
private void validateRequest(Request request, Application application, AuthCategory category) throws Exception {
    OnboardingEnvironment environment = configService.getOnboardingEnvironment(request.getEnvironment());

    // Extract group list from metadata
    List<String> groupList = ((ArrayList<String>) JsonUtilHelper.fromJson(request.getMetadata(), ArrayList.class));
    groupList = groupList.stream().filter(Objects::nonNull).collect(Collectors.toList());

    // Setup Graph client
    String clientId = environment.getConfigFlag().getClientId();
    String clientSecret = environment.getConfigFlag().getClientSecret();
    GraphServiceClient client = GraphServiceClient.builder()
            .authenticationProvider(new ClientCredentialBuilder(clientId, clientSecret).build())
            .buildClient();

    // Validate entityIds
    StringBuilder str = new StringBuilder();
    String url = "https://graph.microsoft.com/v1.0/myorganization/applications?$select=id,identifierUris,displayName&$count=true&$top=500";

    do {
        ApplicationCollectionResponse result = client.applications().withUrl(url).get();
        List<MicrosoftGraphApplication> appList = result.getValue();

        appList.stream()
                .filter(a -> a.getId().equals(application.getMetadata()))
                .forEach(a -> str.append(a.getIdentifierUris().stream()
                        .reduce((a1, a2) -> a1 + "," + a2)));

        if (str.length() > 0) {
            str.append("These entityIds are already present: ").append(str);
            break;
        }
    } while (url != null && url.length() > 0);

    // Check for duplicate groups
    List<String> groupsWithoutDup = new ArrayList<>();
    int length = 0;
    int pos = 0;
    String temp = "";

    if (groupsWithoutDup.size() > 0) {
        List<String> tempGroups = groupsWithoutDup.stream()
                .filter(g -> g != null && !g.isEmpty())
                .collect(Collectors.toList());
        if (!tempGroups.isEmpty()) {
            str.append("Duplicate groups found");
        }
    }
}
/**
 * Update groups in onboarding request DTO
 */
private void updateGroups(OnboardingRegisterRequestDTO requestDto) {
    Map<String, Object> param = requestDto.getAppParameters();
    String[] tmpKeys = "appDefinition.appInstanceDefinition.authentication".split("\\.");
    Map<String, Object> currentMap = param;

    for (int i = 0; i < tmpKeys.length; i++) {
        currentMap = (Map<String, Object>) currentMap.get(tmpKeys[i]);
    }

    // Update adAuthzGroups
    List<String> grps = (List<String>) currentMap.get("adAuthzGroups");
    List<String> updatedGrps = grps.stream()
            .flatMap(g -> Arrays.stream(g.split(",")))
            .collect(Collectors.toList());
    currentMap.put("adAuthzGroups", updatedGrps);

    // Reset currentMap for nested attributes
    currentMap = param;
    tmpKeys = "appDefinition.appInstanceDefinition.authentication".split("\\.");
    for (int i = 0; i < tmpKeys.length; i++) {
        currentMap = (Map<String, Object>) currentMap.get(tmpKeys[i]);
    }

    // Handle OpenID attributes
    if (currentMap.containsKey("openIdAttributes")) {
        currentMap = (Map<String, Object>) currentMap.get("openIdAttributes");
        currentMap = (Map<String, Object>) currentMap.get("claimAttributes");
        List<String> authGrps = (List<String>) currentMap.get("groups");
        List<String> updatedAuthGrps = authGrps.stream()
                .flatMap(g -> Arrays.stream(g.split(",")))
                .collect(Collectors.toList());
        currentMap.put("groups", updatedAuthGrps);
    }

    // Handle SAML attributes
    if (currentMap.containsKey("samlAttributes")) {
        currentMap = (Map<String, Object>) currentMap.get("samlAttributes");
        currentMap = (Map<String, Object>) currentMap.get("claimAttributes");
        List<String> authGrps = (List<String>) currentMap.get("groups");
        List<String> updatedAuthGrps = authGrps.stream()
                .flatMap(g -> Arrays.stream(g.split(",")))
                .collect(Collectors.toList());
        currentMap.put("groups", updatedAuthGrps);
    }
}
