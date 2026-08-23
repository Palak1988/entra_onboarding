package com.example.oidc.service.transaction;
package com.sc.mfa.control.adoption.v1.service.transaction;


import java.util.ArrayList;

/**
 * Service to manage system configuration information
 */
@Service
@Slf4j
public class ConfigurableService {

    public static final String ENVIRONMENT_PROD = "PROD";

    @Resource
    private OnboardingEnvironmentRepository environmentRepository;

    @Resource
    private StatusRepository statusRepository;

    @Resource
    private StageRepository stageRepository;

    @Resource
    private RequestRepository requestRepository;

    /**
     * query configuration of env
     *
     * @param name name of env
     * @return configuration of env
     */
    public OnboardingEnvironment findEnvironmentByName(String name) {
        Status status = findStatus(EnvironmentStatus.ACTIVE.name());
        return environmentRepository.findFirstByNameAndStatusId(name, status.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find environment by name " + name));
    }

    /**
     * query configuration of env
     *
     * @param name name of env
     * @return configuration of env
     */
    public OnboardingEnvironment findEnvironmentById(Long id) {
        return environmentRepository.findFirstById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find environment by id " + id));
    }

    /**
     * query status configuration
     *
     * @param status status type
     * @return status configuration
     */
    public Status findStatus(RequestStatus status) {
        return findStatus(status.name());
    }

    /**
     * query status configuration
     *
     * @param status status name
     * @return status configuration
     */
    public Status findStatus(String status) {
        return statusRepository.findFirstByStatus(status)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cannot find status by name " + status));
    }

    /**
     * query status configuration of success
     *
     * @return status configuration
     */
    public Status successStatus() {
        return findStatus(RequestStatus.SUCCESS);
    }

    /**
     * query status configuration of in_progress
     *
     * @return status configuration
     */
    public Status inProgressStatus() {
        return findStatus(RequestStatus.IN_PROGRESS);
    }

    /**
     * query status configuration of failure
     *
     * @return status configuration
     */
    public Status failureStatus() {
        return findStatus(RequestStatus.FAILURE);
    }

    /**
     * query status configuration by id
     *
     * @return status configuration
     */
    public Status findStatus(Long id) {
        return statusRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Cannot find status by id " + id));
    }

    /**
     * query status configuration by id
     *
     * @return status configuration
     */
    public Stage findStage(Long id) {
        return stageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Cannot find stage by id " + id));
    }

    public RequestStatus findStatusEnum(Long id) {
        Status status = findStatus(id);
        return RequestStatus.valueOf(status.getStatus());
    }

    public Stage findStage(RequestStage stage) {
        return stageRepository.findFirstByStage(stage.name()).orElseThrow(() -> new ResourceNotFoundException(
                "Cannot find stage by name " + stage.name()));
    }

    public Stage findStage(String stage) {
        return stageRepository.findFirstByStage(stage).orElseThrow(() -> new ResourceNotFoundException(
                "Cannot find stage by name " + stage));
    }

    public Stage stageOfRequested() {
        RequestStage requested = RequestStage.REQUESTED;
        return stageRepository.findFirstByStage(requested.name()).orElseThrow(() -> new ResourceNotFoundException(
                "Cannot find stage of " + requested.name()));
    }

    public Stage mapStage(Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new ResourceNotFoundException(
                "Cannot find request by id " + requestId));

        String environmentName = String.valueOf(request.getMetadata().get("entraEnvironment"));
        if (StringUtils.containsIgnoreCase(environmentName, ENVIRONMENT_PROD)) {
            return findStage(RequestStage.PRE_PROD);
        }
        return findStage(RequestStage.NON_PROD_SETUP_IDP);
    }

    public Status getStatus(OnboardingRegisterRequestDto requestDto) {
        Status status = null;
        if (requestDto.getParameters().get(AppConstants.STAGE).equals(RequestStage.DRAFT.name()) ||
                requestDto.getParameters().get(AppConstants.STAGE).equals(RequestStage.SUBMITTED_FOR_APPROVAL.name())) {
            status = findStatus(RequestStatus.REQUESTED.name());
        } else if (requestDto.getParameters().get(AppConstants.STAGE).equals(RequestStage.APPROVED.name())) {
            status = findStatus(RequestStatus.IN_PROGRESS.name());
        }
        return status;
    }

    public String[] getStringArrayFromObject(Request request, String path) {
        ArrayList<String> arrayGroup = (ArrayList<String>) JsonUtil.getValueFromMap(request.getMetadata(), path);
        return arrayGroup == null ? null : ArrayUtils.toArray(
                Objects.requireNonNull(arrayGroup).toArray(), arrayGroup.size(), String[].class);
    }
}