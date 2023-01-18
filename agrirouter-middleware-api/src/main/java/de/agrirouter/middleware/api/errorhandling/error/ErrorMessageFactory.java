package de.agrirouter.middleware.api.errorhandling.error;

/**
 * Factory to create error messages.
 */
public final class ErrorMessageFactory {

    private ErrorMessageFactory() {
    }

    public static ErrorMessage couldNotFindApplication() {
        return new ErrorMessage(ErrorKey.APPLICATION_NOT_FOUND, "Could not find the application.");
    }

    public static ErrorMessage onboardRequestFailed() {
        return new ErrorMessage(ErrorKey.ONBOARD_REQUEST_FAILED, "The onboard request failed.");
    }

    public static ErrorMessage couldNotConnectMqttClient(String agrirouterEndpointId, Throwable t) {
        return new ErrorMessage(ErrorKey.COULD_NOT_CONNECT_MQTT_CLIENT, String.format("Could not connect the MQTT client for the given endpoint with the ID '%s'. The root cause was '%s'.", agrirouterEndpointId, t.getMessage()));
    }

    public static ErrorMessage couldNotConnectMqttClient(String agrirouterEndpointId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_CONNECT_MQTT_CLIENT, String.format("Could not connect the MQTT client for the given endpoint with the ID '%s'.", agrirouterEndpointId));
    }

    public static ErrorMessage couldNotParseOnboardResponse() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PARSE_ONBOARD_RESPONSE, "Could not parse onboard response from JSON.");
    }

    public static ErrorMessage couldNotFindEndpoint() {
        return new ErrorMessage(ErrorKey.ENDPOINT_NOT_FOUND, "Could not find endpoint by the given ID.");
    }

    public static ErrorMessage couldNotFindContentMessage() {
        return new ErrorMessage(ErrorKey.CONTENT_MESSAGE_NOT_FOUND, "Could not find the content message by the given ID.");
    }

    public static ErrorMessage couldNotFindMessageWaitingForAcknowledgement(String messageId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_FIND_MESSAGE_WAITING_FOR_ACKNOWLEDGEMENT, String.format("Could not find message with message id '%s' waiting for acknowledgement.", messageId));
    }

    public static ErrorMessage middlewareDoesNotSupportGateway(String gatewayId) {
        return new ErrorMessage(ErrorKey.MIDDLEWARE_DOES_NOT_SUPPORT_GATEWAY, String.format("The middleware does not support the gateway with the ID '%s'.", gatewayId));
    }

    public static ErrorMessage invalidParameterForAction(String... names) {
        return new ErrorMessage(ErrorKey.INVALID_PARAMETER_FOR_ACTION, String.format("The following parameters are in an error state, please check >>> [%s]", String.join(",", names)));
    }

    public static ErrorMessage tenantAlreadyExists(String name) {
        return new ErrorMessage(ErrorKey.TENANT_ALREADY_EXISTS, String.format("A tenant with the name '[%s]' already exists.", name));
    }

    public static ErrorMessage couldNotFindTenant(String id) {
        return new ErrorMessage(ErrorKey.COULD_NOT_FIND_TENANT, String.format("A tenant with the ID '[%s]' was not found.", id));
    }

    public static ErrorMessage couldNotFindDescriptorForTheTimeLog() {
        return new ErrorMessage(ErrorKey.COULD_NOT_FIND_DESCRIPTOR_FOR_TIME_LOG, "Could not find the descriptor for the time log entry.");
    }

    public static ErrorMessage couldNotParseTaskData() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PARSE_TASK_DATA, "Could not parse task data file.");
    }

    public static ErrorMessage applicationDoesNotSupportSecuredOnboarding() {
        return new ErrorMessage(ErrorKey.SECURED_ONBOARD_PROCESS_NOT_SUPPORTED, "The application does not support secured onboarding.");
    }

    public static ErrorMessage couldNotFindVirtualEndpoint() {
        return new ErrorMessage(ErrorKey.ENDPOINT_NOT_FOUND, "The virtual endpoint was not found.");
    }

    public static ErrorMessage couldNotParseDeviceDescription() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PARSE_DEVICE_DESCRIPTION, "Could not parse the given device description. Please check the format.");
    }

    public static ErrorMessage couldNotParseTimeLog() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PARSE_TIME_LOG, "Could not parse the given time log. Please check the format.");
    }

    public static ErrorMessage endpointWithTheSameExternalIdIsPresent(String externalEndpointId) {
        return new ErrorMessage(ErrorKey.ENDPOINT_ALREADY_EXISTING, String.format("Could not create the endpoint, since the external endpoint ID '%s' does already exist.", externalEndpointId));
    }

    public static ErrorMessage applicationDoesAlreadyExist(String applicationId, String versionId) {
        return new ErrorMessage(ErrorKey.APPLICATION_ALREADY_EXISTING, String.format("The application with the id '%s' does already exist with the version '%s'.", applicationId, versionId));
    }

    public static ErrorMessage couldnotFindTeamSet(String teamSetContextId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_FIND_TEAM_SET, String.format("Could not find the team set with the id '%s'.", teamSetContextId));
    }

    public static ErrorMessage switchingAccountsWhenReonboardingIsNotAllowed() {
        return new ErrorMessage(ErrorKey.SWITCHING_ACCOUNTS_WHEN_REONBOARDING_IS_NOT_ALLOWED, "Switching accounts when performing the onboard process for an existing endpoint is not allowed.");
    }

    public static ErrorMessage missingRouterDeviceForApplication() {
        return new ErrorMessage(ErrorKey.MISSING_ROUTER_DEVICE_FOR_APPLICATION, "The router device is missing, can not establish communication using router devices.");
    }

    public static ErrorMessage missingFilterCriteriaForTimeLogSearch() {
        return new ErrorMessage(ErrorKey.MISSING_FILTER_CRITERIA_FOR_TIME_LOG_SEARCH, "There has to be a criteria for searching for time logs. Either the time log period has to be given or there has to be a time interval.");
    }

    public static ErrorMessage parameterValidationProblem() {
        return new ErrorMessage(ErrorKey.PARAMETER_VALIDATION_PROBLEM, "There was an error while validating the parameters for the request.");
    }

    public static ErrorMessage couldNotAssembleChunks() {
        return new ErrorMessage(ErrorKey.COULD_NOT_ASSEMBLE_CHUNKS, "Could not assemble the chunks for the message, the chunk context ID is invalid or there was a problem while merging the chunks.");
    }

    public static ErrorMessage teamSetContextIdAlreadyInUse(String teamSetContextId) {
        return new ErrorMessage(ErrorKey.TEAM_SET_CONTEXT_ID_ALREADY_IN_USE, String.format("The team set context ID '%s' is already in use.", teamSetContextId));
    }

    public static ErrorMessage couldNotCreatePrivateKeyForApplication(String applicationId, String versionId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_CREATE_PRIVATE_KEY_FOR_APPLICATION, String.format("Could not create private key for application with id '%s' and version '%s'.", applicationId, versionId));
    }

    public static ErrorMessage couldNotCreatePublicKeyForApplication(String applicationId, String versionId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_CREATE_PUBLIC_KEY_FOR_APPLICATION, String.format("Could not create public key for application with id '%s' and version '%s'.", applicationId, versionId));
    }

    public static ErrorMessage agrirouterStatusNotOperational() {
        return new ErrorMessage(ErrorKey.AGRIROUTER_STATUS_NOT_OPERATIONAL, "Current agrirouter© status is not operational.");
    }

    public static ErrorMessage agrirouterStatusNotAvailable() {
        return new ErrorMessage(ErrorKey.AGRIROUTER_STATUS_NOT_AVAILABLE, "Current agrirouter© status is not available.");
    }

    public static ErrorMessage notAuthorized() {
        return new ErrorMessage(ErrorKey.NOT_AUTHORIZED, "Nice try, but the user is not authorized to perform this action.");
    }
}