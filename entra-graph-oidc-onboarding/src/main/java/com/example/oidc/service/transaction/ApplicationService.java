package com.example.oidc.service.transaction;
package com.sc.mfa.control.adoption.v1.service.transaction;

import java.time.LocalDateTime;

/**
 * Service to manage application
 */
@Service
@Slf4j
public class ApplicationService {

    @Autowired
    GraphService graphService;

    @Autowired
    HashicorpService hashicorpService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private ApplicationRepository applicationRepository;

    @Resource
    private RequestRepository requestRepository;

    @Resource
    private ConfigurableService configurableService;

    @Resource
    private OnboardingEnvironmentRepository environmentRepository;

    /**
     * verify if application has been onboarded already
     *
     * @param instanceId application instance id
     * @param env        env of onboarding
     * @return <code>true</code> onboarded successfully in env
     */
    public boolean isSuccessApplication(String instanceId, String env) {
        OnboardingEnvironment environment = configurableService
                .findEnvironmentByName(env);

        Optional<Application> applicationOptional = applicationRepository
                .findByInstanceIdAndEnvironmentId(instanceId, environment.getId());

        if (applicationOptional.isEmpty()) {
            return false;
        }

        Application application = applicationOptional.get();
        Status status = configurableService.findStatus(application.getStatusId());

        return RequestStatus.SUCCESS.isStatus(status.getStatus());
    }

    /**
     * save/update application with request
     *
     * @param requestId request id of Request table
     * @param authCategory auth category of onboarding
     * @return application in database
     */
    public Application process(Request request, AuthCategory authCategory, Request completedReq) {

        String mfaFlag = null;
        int sbiRating = 0;

        OnboardingEnvironment environment = configurableService
                .findEnvironmentByName(String.valueOf(JsonUtil.getValueFromMap(request.getMetadata(),
                        OnboardingProperties.requestMetadata.getCommon().getEntraEnvironment())));

        Application application = applicationRepository.findByRequestId(request.getRequestId())
                .orElseGet(() -> new Application());

        if (completedReq != null) {
            Application completedApplication = applicationRepository.findFirstByRequestId(completedReq.getId());
            BeanUtils.copyProperties(completedApplication, application);

            application.setTargetId(completedApplication.getTargetId());
            application.setAppId(completedApplication.getAppId());
            application.setTargetServicePrincipalId(completedApplication.getTargetServicePrincipalId());
            application.setTargetClaimsMappingPolicyId(completedApplication.getTargetClaimsMappingPolicyId());
            completedApplication.setStatusId(configurableService.findStatus(RequestStatus.HOLD).getId());
            applicationRepository.saveAndFlush(completedApplication);
        }

        if (Objects.nonNull(application.getStatusId())) {
            Status status = configurableService.findStatus(application.getStatusId());
            String statusName = status.getStatus();

            if (RequestStatus.IN_PROGRESS.isStatus(statusName)) {
                throw new InvalidParameterException("application status is IN_PROGRESS");
            }
        }

        application.setIamId(((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getIamId())));
        application.setInstanceId((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getInstanceId()));
        application.setInstanceName((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getInstanceName()));

        String appName = String.valueOf(JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getAppName()));
        String sbiRating = String.valueOf(JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getSbiRating()));

        try {
            if (!sbiRating.equals(AppConstants.NULL_STRING)) {
                sbiRating = Integer.parseInt(sbiRating);
                application.setAppSbiRating(sbiRating);
            }
        } catch (Exception e) {
            log.error("Invalid sbiRating");
        }

        application.setApplicationAccessType((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getApplicationAccessType()));
        application.setDisplayName((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getDisplayName()));
        application.setApplicationDeploymentType((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getApplicationDeploymentType()));

        mfaFlag = (String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getMfaEnablement());

        mfaFlag = (sbiRating == 4 || sbiRating == 5) ? AppConstants.YES_FLAG : mfaFlag;

        application.setMfaFlagValue(
                (sbiRating == 4 || sbiRating == 5)
                        ? (AppConstants.NO_FLAG.equalsIgnoreCase(mfaFlag)
                        ? AppConstants.YES_FLAG : AppConstants.NO_FLAG)
                        : mfaFlag);

        application.setSetAuthType((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getAuthType()));

        application.setAuthValidationLoginUrl((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getAuthValidationLoginUrl()));

        application.setIamInstanceJob((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getIamInstanceJob()));

        application.setCreatedDate(Objects.isNull(application.getCreatedDate())
                ? SystemUtil.currentDateTime()
                : application.getCreatedDate());

        application.setEnvironmentId(environment.getId());

        application.setIamName((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getIamName()));

        application.setApproverComment(configurableService.getAuthzFromGraph(request,
                OnboardingProperties.requestMetadata.getCommon().getApproverComment()));

        application.setSignInFrequency((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getSignInFrequency()));

        application.setAuthenticationStrength((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getAuthenticationStrength()));

        application.setWorkload((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getWorkload()));

        application.setCrmRefId((String) JsonUtil.getValueFromMap(request.getMetadata(),
                OnboardingProperties.requestMetadata.getCommon().getCrmRefId()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        application
                .setPlannedGoLiveDate(LocalDateTime.parse(
                        String.valueOf(JsonUtil.getValueFromMap(request.getMetadata(),
                                OnboardingProperties.requestMetadata.getCommon().getPlannedGoLiveDate())),
                        formatter));

        application
                .setGoLiveDate(LocalDateTime.parse(
                        String.valueOf(JsonUtil.getValueFromMap(request.getMetadata(),
                                OnboardingProperties.requestMetadata.getCommon().getGoLiveDate())),
                        formatter));

        application.setRequestId(request.getId());
        application.setTrackingId(request.getTrackingId());
        application.setStageId(request.getStageId());
        application.setStatusId(request.getStatusId());
        application.setIntegrationType(authCategory.name());
        application.setModifiedDate(SystemUtil.currentDateTime());
        application.setErrorMessages("");

        Application app = applicationRepository.saveAndFlush(application);

        CompletableFuture.runAsync(() -> validateGroup(app, request, authCategory));

        applicationContext.publishEvent(new ApplicationProcessEvent(application.getId()));

        return app;
    }

    public void validateGroup(Application app, Request request, AuthCategory category) {
        try {
            List<String> groupList = ((ArrayList<String>) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getCommon().getADAuthzGroups()));

            groupList.addAll(category.name().equals(AuthCategory.SAML.name())
                    ? ((ArrayList<String>) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getSaml().getGroups()))
                    : ((ArrayList<String>) JsonUtil.getValueFromMap(request.getMetadata(),
                    OnboardingProperties.requestMetadata.getOidc().getGroups())));

            validateEntraData(groupList.toArray(String[]::new), app.getEnvironmentId());

        } catch (Exception e) {
            request.setStageId(configurableService.findStage(RequestStage.FAILED).getId());
            request.setStatusId(configurableService.failureStatus().getId());
            request.setModifiedDate(SystemUtil.currentDateTime());
            requestRepository.saveAndFlush(request);

            Application appl = findApplication(request.getTrackingId());
            appl.setStageId(configurableService.findStage(RequestStage.FAILED).getId());
            appl.setStatusId(configurableService.findStatus(RequestStatus.FAILURE).getId());
            appl.setErrorMessages(e.getMessage());
            applicationRepository.saveAndFlush(appl);
        }
    }

    /**
     * query application by id
     *
     * @param id application id
     * @return application
     */
    public Application getById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find application by id " + id));
    }

    /**
     * update application status and stage
     *
     * @param applicationId id of application
     * @param stage stage name
     * @param status status name
     */
    public Application updateStageOrStatus(Long applicationId, String stage, RequestStatus status,
                                           Map<String, Object> comments) {
        Application application = getById(applicationId);

        if (null != comments) {
            application.setApproverComment(comments);
        }

        if (null == stage) {
            stage = configurableService.findStage(application.getStageId()).getStage();
        }

        if (environmentRepository.findOnboardingEnvironmentById(application.getEnvironmentId()).getName()
                .equals(AppConstants.PROD_TENANT)
                && stage.equals(RequestStage.APPROVED.name())
                && configurableService.findStage(application.getStageId()).getStage()
                .equals(RequestStage.SUBMITTED_FOR_APPROVAL.name())) {
            stage = RequestStage.SUBMITTED_FOR_APPROVAL_PSS.name();
        } else if (stage.equals(RequestStage.SUBMITTED_FOR_APPROVAL_PSS.name())
                || stage.equals(RequestStage.REJECTED.name())
                || stage.equals(RequestStage.CANCELLED.name())) {
            status = RequestStatus.SUCCESS;
        }

        if (stage != null) {
            application.setStageId(
                    configurableService.findStage(RequestStage.valueOf(stage)).getId());
        }

        if (status != null) {
            application.setStatusId(configurableService.findStatus(status).getId());
        }

        if (stage != null && stage.equals(RequestStage.CANCELLED.name())) {
            application.setTargetId(null);
            application.setAppId(null);
            application.setTargetServicePrincipalId(null);
            application.setTargetClaimsMappingPolicyId(null);

            String instanceId = application.getInstanceId();

            List<Application> apps = applicationRepository.findByInstanceId(instanceId).stream()
                    .filter(a -> a.getStageId().equals(
                            configurableService.findStage(RequestStage.COMPLETED).getId())
                            && a.getStatusId().equals(
                            configurableService.findStatus(RequestStatus.HOLD).getId()))
                    .collect(Collectors.toList());

            apps.forEach(a -> {
                a.setStatusId(configurableService.findStatus(RequestStatus.SUCCESS).getId());
                applicationRepository.saveAndFlush(a);
            });
        }

        application.setModifiedDate(SystemUtil.currentDateTime());

        return applicationRepository.saveAndFlush(application);
    }

    public Application updateStageOrStatus(Long applicationId, String stage, RequestStatus status) {
        return updateStageOrStatus(applicationId, stage, status, null);
    }

    /**
     * update onboarding result to application
     *
     * @param id application id
     * @param command onboarding result
     */
    @Transactional
    public void updateApplication(Long id, OnboardingCommand command) {
        Application app = getById(id);

        app.setTargetId((String) command.getResult()
                .get(com.sc.mfa.control.adoption.common.OnboardingResultKey.OBJECT_ID.getKey()).toString());

        app.setTargetAppId((String) command.getResult()
                .get(com.sc.mfa.control.adoption.common.OnboardingResultKey.APP_ID.getKey()).toString());

        app.setTargetServicePrincipalId((String) command.getResult()
                .get(com.sc.mfa.control.adoption.common.OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey()).toString());

        app.setTargetClaimsMappingPolicyId((String) command.getResult()
                .get(com.sc.mfa.control.adoption.common.OnboardingResultKey.CLAIM_POLICY_ID.getKey()).toString());

        app.setModifiedDate(SystemUtil.currentDateTime());
        app.setStageId(configurableService.findStage(RequestStage.COMPLETED).getId());
        app.setStatusId(configurableService.findStatus(RequestStatus.SUCCESS).getId());
        app.setErrorMessages("");

        String instanceId = app.getInstanceId();

        List<Application> apps = applicationRepository.findByInstanceId(instanceId).stream()
                .filter(a -> a.getStageId().equals(
                        configurableService.findStage(RequestStage.COMPLETED).getId())
                        && a.getStatusId().equals(
                        configurableService.findStatus(RequestStatus.HOLD).getId()))
                .collect(Collectors.toList());

        apps.forEach(a -> {
            a.setStageId(configurableService.findStage(RequestStage.APPLICATION_DELETE_REQUEST).getId());
            a.setStatusId(configurableService.findStatus(RequestStatus.SUCCESS).getId());
            applicationRepository.saveAndFlush(a);
        });

        applicationRepository.saveAndFlush(app);
    }

    /**
     * query application id by tracking id
     *
     * @param trackingId tracking id from sch
     * @return application in db
     */
    public Application findApplication(String trackingId) {
        return applicationRepository.findFirstByTrackingId(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("trackingId %s not found", trackingId)));
    }

    /**
     * query application id by tracking id
     *
     * @param requestId tracking id from sch
     * @return application in db
     */
    public Application findByRequestId(Long requestId) {
        return applicationRepository.findFirstByRequestId(requestId);
    }

    public void retryOption(Exception e, String trackingId, Request retryRequest,
                            ConfigurableService configurableService,
                            RequestRepository requestRepository) {

        if (retryRequest.getExecutedTimes() >= 4) {
            log.error("failed to onboarding to mfa for request [{}]", trackingId, e);

            retryRequest.setStageId(
                    configurableService.findStage(RequestStage.FAILED).getId());

            retryRequest.setStatusId(
                    configurableService.failureStatus().getId());

            retryRequest.setModifiedDate(SystemUtil.currentDateTime());
            requestRepository.saveAndFlush(retryRequest);

            Application app = findApplication(trackingId);

            app.setStageId(
                    configurableService.findStage(RequestStage.FAILED).getId());

            app.setStatusId(
                    configurableService.findStatus(RequestStatus.FAILURE).getId());

            app.setErrorMessages(e.getMessage());

            applicationRepository.saveAndFlush(app);
        }
    }

    // delete group need to do
    public void validateEntraData(String[] groups, Long envId) throws Exception {

        OnboardingEnvironment environment =
                configurableService.findEnvironmentById(envId);

        String clientEntry =
                hashicorpService.getEnvironmentData(environment);

        String[] groupsWithoutDup =
                Arrays.stream(groups).distinct().toArray(String[]::new);

        final ClientSecretCredential credential =
                new ClientSecretCredentialBuilder()
                        .clientId(environment.getEnvConfig().getClientId())
                        .tenantId(environment.getEnvConfig().getTenantId())
                        .clientSecret(clientEntry)
                        .build();

        List<Group> entraGroups = Objects.requireNonNull(
                graphService.getGroupsWithGraphService(
                        new GraphServiceClient(credential),
                        AppFilterRules.getGroupsRequestBuilder(groupsWithoutDup))
                        .getValue());

        if (groupsWithoutDup.length > 0
                && entraGroups.size() != groupsWithoutDup.length) {

            throw new InvalidParameterException(
                    "Provided group not exist in Azure AD - "
                            + Arrays.asList(groupsWithoutDup).stream()
                            .filter(a -> !entraGroups.stream()
                                    .map(e -> e.getDisplayName())
                                    .collect(Collectors.toList())
                                    .contains(a))
                            .reduce((a, b) -> {
                                return a + "," + b;
                            }).orElse(""));
        }
    }
}