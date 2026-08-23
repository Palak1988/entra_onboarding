package com.example.oidc.service.generic;
package com.sc.mfa.control.adoption.v1.service.generic;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.example.oidc.AppConstantants.OnboardingResultKey;
import com.example.oidc.config.OnboardingProperties;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.ApplicationCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;

public class CommonService extends GraphService {
    CommonUtil commonUtil;

    public CommonService(GraphServiceClient graphServiceClient) {
        this.graphServiceClient = graphServiceClient;
        commonUtil = new CommonUtil();
    }

    public OnboardingCommand getDisplayNameExistingAppIds(OnboardingCommand command) {
        // Clean the failed entries to onboard new application
        if (Objects.nonNull(command.getTargetAppId()) && command.getTargetAppId().equals("")) {
            command.setResultKey(OnboardingResultKey.OBJECT_ID.toString(), command.getTargetAppId());
            command.setResultKey(OnboardingResultKey.APP_ID.toString(), command.getTargetAppId());
            command.setResultKey(OnboardingResultKey.SERVICE_PRINCIPAL_ID.toString(),
                                 command.getTargetServicePrincipalId());
        }

        if (Objects.nonNull(command.getTargetAppId()) && command.getTargetAppId().equals("")) {
            // Check application present or not
            ApplicationCollectionResponse applicationCollectionResponse = getApplicationCollectionResponseAppId(
                    command.getTargetAppId());
            if (applicationCollectionResponse.getValue().size() > 0) {
                Objects.nonNull(applicationCollectionResponse.getValue().get(0).getAppRoles());
                command.setResultKey(OnboardingResultKey.APP_ROLE_ID.toString(),
                                     commonUtil.getAppRoleId(applicationCollectionResponse.getValue().get(0).getAppRoles()));
            }
        }

        return command;
    }

    // delete group need to do
    public void assignGroupsToServicePrincipal(String servicePrincipalId, String roleId, String[] groups) {
        AppRoleAssignment appRoleAssignment = new AppRoleAssignment();
        AppRoleAssignmentForSP appRoleAssignmentForSP = new AppRoleAssignmentForSP();

        if (AppRoleAssignment.appRoleAssignment != null) {
            appRoleAssignment = appRoleAssignmentForSP.getAppRoleAssignmentForSP(servicePrincipalId, appRoleAssignment.getId());
        }

        List<Groups> gs = getGroups(groups);
        for (Groups g : gs) {
            // get group id
            Group gs1 = g;
            // AuthorizationGroups or default
            for (Groups gs1 : gs) {
                // get group id
                // Identifier (id) for the app role which is assigned to the resource application
                // If the resource application has not declared any app roles, a default app role ID of 00000000-0000-0000-0000-000000000000
                // can be specified to signal that the principal is assigned to the resource app without any specific app roles.
                // Required on create.
                // principal is assigned to the resource app without any specific app roles.
// Required on create.
postAppRoleAssignment(servicePrincipalId,
    CommonUtil.appRoleAssignment(group.getId(), servicePrincipalId, roleId));

public void assignGroupForApp(OnboardingCommand command, String appRoleId) {
    List<String> groupList = ((ArrayList) JsonUtil.getValueFromMap(command.getMetadata(),
        OnboardingProperties.requestMetadata.getCommon().getEduAuthzGroups()));
    if (groupList != null && !groupList.isEmpty()) {
        String[] groups = groupList.toArray(new String[groupList.size()]);
        assignGroupsToServicePrincipal(
            String.valueOf(command.getResult().get(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey())),
            appRoleId, groups);
    }
}

public void assignClaimsMappingPolicy(OnboardingCommand command) {
    com.microsoft.graph.models.ReferenceCreate referenceCreate = new com.microsoft.graph.models.ReferenceCreate();
    referenceCreate.setOdataId(AppConstants.CLAIMS_MAPPING_POLICY_URL
        + command.getResult().get(OnboardingResultKey.CLAIM_POLICY_ID.getKey()).toString());
    assignClaimsMappingPolicy(referenceCreate,
        command.getResult().get(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey()).toString());
}

@Retryable(maxAttempts = 5, backoff = @Backoff(maxDelay = 20000L, random = true), retryFor = Exception.class)
public void deleteApplication(OnboardingCommand command) throws ObataError, InterruptedException {
    if (Objects.nonNull(command.getResult().get(OnboardingResultKey.OBJECT_ID.getKey()))) {
        deleteApplication(String.valueOf(command.getResult().get(OnboardingResultKey.OBJECT_ID.getKey())));
    }
}

public void customSecurityAttribute(OnboardingCommand command) {
    ServicePrincipal servicePrincipal = new ServicePrincipal();
    CustomSecurityAttributeValue customSecurityAttributeValue =
        new CustomSecurityAttributeValue();
    HashMap<String, Object> additionalData = new HashMap<>();
    Object additionalEngineeringProperties = new UntypedObject(new HashMap<>(){
        {
             put(AppConstants.DATA_TYPE_KEY, new TypedString(AppConstants.DATA_TYPE_VALUE));
    put(AppConstants.SBIA_KEY, new TypedString(OnboardingProperties.requestMetadata.getCommon().getAppSbiaRating()));
    put(AppConstants.APP_ACCESS_TYPE_KEY, new TypedString(OnboardingProperties.requestMetadata.getCommon().getAppAccessType()));
    put(AppConstants.CIID_KEY, new TypedString(OnboardingProperties.requestMetadata.getCommon().getAppCalculationId()));
    put(AppConstants.APP_INSTANCE_ID_KEY, new TypedString(OnboardingProperties.requestMetadata.getCommon().getAppInstanceId()));
    put(AppConstants.REQUESTED_VALUE_KEY, new TypedString(OnboardingProperties.requestMetadata.getCommon().getAppSbiaRating()));

    int sbiaRating = 0;
    if (!AppConstants.NULL_STRING.equals(sbiaRating)) {
        sbiaRating = Integer.parseInt(sbiaRating);
    }

    String mfaFlag = commonUtil.getMetadataValue(command,OnboardingProperties.requestMetadata.getCommon().getMfaEnablement());
    mfaFlag = (sbiaRating == 4 || sbiaRating == 5) ? AppConstants.YES_FLAG : mfaFlag;
    String overrideDefaultAuthType = (sbiaRating == 4 || sbiaRating == 5) ? (AppConstants.NO_FLAG.equalsIgnoreCase(mfaFlag) ? AppConstants.YES_FLAG :AppConstants.NO_FLAG) : mfaFlag; 
    put(AppConstants.OVERRIDE_DEFAULT_AUTH_TYPE_KEY, new UntypedString(!overrideDefaultAuthType.equals(AppConstants.NULL_STRING) && !overrideDefaultAuthType.isEmpty()) ? overrideDefaultAuthType : AppConstants.NO_FLAG);
    put(AppConstants.AUTHENTICATION_STRENGTH, new UntypedString(commonUtil.getMetadataValue(command, OnboardingProperties.requestMetadata.getCommon().getAuthenticationStrength())));
    put(AppConstants.SIGN_IN_FREQUENCY, new UntypedString(commonUtil.getMetadataValue(command, OnboardingProperties.requestMetadata.getCommon().getSignInFrequency())));
        }
    });

   additionalData.put(AppConstants.ASMFA_VALUE, additionalEngineeringProperties);
   customSecurityAttributes.setAdditionalData(additionalData);
   servicePrincipal.setCustomSecurityAttributes(customSecurityAttributes);

   patchCustomSecurityAttributes(command.getResult().get(OnboardingResultKey.SERVICE_PRINCIPAL_ID.getKey()).toString(), servicePrincipal);

}

public void CreateOrUpdateClaimsMappingPolicy(OnboardingCommand command, String templateFolderName,
        DatabaseTemplateParser databaseTemplateParser) throws IOException {
    Map<String, Object> definition = databaseTemplateParser
            .parse(templateFolderName.toUpperCase() + ":ClaimPolicyDefinition", command);
    ClaimsMappingPolicy claimsMappingPolicy = new ClaimsMappingPolicy();
    LinkedList<String> claimsMappingPolicyList = new LinkedList<>();
    claimsMappingPolicyList.add(JsonUtil.toJson(definition));
    claimsMappingPolicy.setDefinition(claimsMappingPolicyList);
    claimsMappingPolicy.setName(command.getMetadata().getCommon().getPolicyName());
    String claimsMappingPolicyId = onboardingResultKey.SERVICE_PRINCIPAL_ID.getKey().toString();
    ClaimsMappingPolicy existingClaimsMappingPolicy = getClaimsMappingPolicy(claimsMappingPolicyId);
    if (Objects.nonNull(existingClaimsMappingPolicy)) {
        claimsMappingPolicy.setId(existingClaimsMappingPolicy.getId());
        patchClaimsMappingPolicy(claimsMappingPolicy);
    } else {
        createClaimsMappingPolicy(claimsMappingPolicy);
    }
}

public List<Group> getGroups(String groups[]) {
    log.info("Fetching groups in chunk of 10 at a time");
    int pos = 0;
    int length = 0;
    List<Group> grps = new ArrayList<>();
    while (pos < groups.length) {
        length = groups.length - pos > 10 ? 10 : groups.length - pos;
        String temp[] = new String[length];
        System.arraycopy(groups, pos, temp, 0, length);
        log.info("Fetching groups from pos " + pos + " to " + pos + length);
        grps.addAll(Objects.requireNonNull(getGroups(AppFilterRules.getGroupsRequestBuilder(temp)).getValue()));
        pos = pos + length;
    } return grps;
}

}

            }
        }
    }
}
