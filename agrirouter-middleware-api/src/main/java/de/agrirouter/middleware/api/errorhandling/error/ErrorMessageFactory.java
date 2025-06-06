package de.agrirouter.middleware.api.errorhandling.error;

import org.springframework.http.HttpStatus;

/**
 * Factory to create error messages.
 */
public final class ErrorMessageFactory {

    private ErrorMessageFactory() {
    }

    public static ErrorMessage couldNotFindApplication() {
        return new ErrorMessage(ErrorKey.APPLICATION_NOT_FOUND, "Could not find the application.", HttpStatus.NOT_FOUND);
    }

    public static ErrorMessage onboardRequestFailed() {
        return new ErrorMessage(ErrorKey.ONBOARD_REQUEST_FAILED, "The onboard request failed.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage couldNotConnectMqttClient(String agrirouterEndpointId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_CONNECT_MQTT_CLIENT, String.format("Could not connect the MQTT client for the given endpoint with the ID '%s'.", agrirouterEndpointId), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage couldNotParseOnboardResponse() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PARSE_ONBOARD_RESPONSE, "Could not parse onboard response from JSON.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage couldNotFindEndpointByAgrirouterId(String agrirouterId) {
        return new ErrorMessage(ErrorKey.ENDPOINT_NOT_FOUND, String.format("Could not find endpoint by the agrirouter ID: %s", agrirouterId), HttpStatus.NOT_FOUND);
    }

    public static ErrorMessage couldNotFindContentMessage() {
        return new ErrorMessage(ErrorKey.CONTENT_MESSAGE_NOT_FOUND, "Could not find the content message by the given ID.", HttpStatus.NOT_FOUND);
    }

    public static ErrorMessage couldNotFindMessageWaitingForAcknowledgement(String messageId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_FIND_MESSAGE_WAITING_FOR_ACKNOWLEDGEMENT, String.format("Could not find message with message id '%s' waiting for acknowledgement.", messageId), HttpStatus.NOT_FOUND);
    }

    public static ErrorMessage middlewareDoesNotSupportGateway(String gatewayId) {
        return new ErrorMessage(ErrorKey.MIDDLEWARE_DOES_NOT_SUPPORT_GATEWAY, String.format("The middleware does not support the gateway with the ID '%s'.", gatewayId), HttpStatus.BAD_REQUEST);
    }

    public static ErrorMessage invalidParameterForAction(String... names) {
        return new ErrorMessage(ErrorKey.INVALID_PARAMETER_FOR_ACTION, String.format("The following parameters are in an error state, please check >>> [%s]", String.join(",", names)), HttpStatus.BAD_REQUEST);
    }

    public static ErrorMessage tenantAlreadyExists(String name) {
        return new ErrorMessage(ErrorKey.TENANT_ALREADY_EXISTS, String.format("A tenant with the name '[%s]' already exists.", name), HttpStatus.CONFLICT);
    }

    public static ErrorMessage couldNotFindTenant(String id) {
        return new ErrorMessage(ErrorKey.COULD_NOT_FIND_TENANT, String.format("A tenant with the ID '[%s]' was not found.", id), HttpStatus.NOT_FOUND);
    }

    public static ErrorMessage couldNotFindDescriptorForTheTimeLog() {
        return new ErrorMessage(ErrorKey.COULD_NOT_FIND_DESCRIPTOR_FOR_TIME_LOG, "Could not find the descriptor for the time log entry.", HttpStatus.NOT_FOUND);
    }

    public static ErrorMessage couldNotParseTaskData() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PARSE_TASK_DATA, "Could not parse task data file.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage applicationDoesNotSupportSecuredOnboarding() {
        return new ErrorMessage(ErrorKey.SECURED_ONBOARD_PROCESS_NOT_SUPPORTED, "The application does not support secured onboarding.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage couldNotParseDeviceDescription() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PARSE_DEVICE_DESCRIPTION, "Could not parse the given device description. Please check the format.", HttpStatus.BAD_REQUEST);
    }

    public static ErrorMessage couldNotParseTimeLog() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PARSE_TIME_LOG, "Could not parse the given time log. Please check the format.", HttpStatus.BAD_REQUEST);
    }

    public static ErrorMessage endpointWithTheSameExternalIdIsPresent(String externalEndpointId) {
        return new ErrorMessage(ErrorKey.ENDPOINT_ALREADY_EXISTING, String.format("Could not create the endpoint, since the external endpoint ID '%s' does already exist.", externalEndpointId), HttpStatus.CONFLICT);
    }

    public static ErrorMessage applicationDoesAlreadyExist(String applicationId, String versionId) {
        return new ErrorMessage(ErrorKey.APPLICATION_ALREADY_EXISTING, String.format("The application with the id '%s' does already exist with the version '%s'.", applicationId, versionId), HttpStatus.CONFLICT);
    }

    public static ErrorMessage couldNotFindTeamSet(String teamSetContextId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_FIND_TEAM_SET, String.format("Could not find the team set with the id '%s'.", teamSetContextId), HttpStatus.NOT_FOUND);
    }

    public static ErrorMessage switchingAccountsWhenReOnboardingIsNotAllowed() {
        return new ErrorMessage(ErrorKey.SWITCHING_ACCOUNTS_WHEN_REONBOARDING_IS_NOT_ALLOWED, "Switching accounts when performing the onboard process for an existing endpoint is not allowed.", HttpStatus.BAD_REQUEST);
    }

    public static ErrorMessage missingRouterDeviceForApplication(String internalApplicationId) {
        return new ErrorMessage(ErrorKey.MISSING_ROUTER_DEVICE_FOR_APPLICATION, String.format("The router device for the application '%s' is missing, can not establish communication using router devices.", internalApplicationId), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage missingFilterCriteriaForTimeLogSearch() {
        return new ErrorMessage(ErrorKey.MISSING_FILTER_CRITERIA_FOR_TIME_LOG_SEARCH, "There has to be a criteria for searching for time logs. Either the time log period has to be given or there has to be a time interval.", HttpStatus.BAD_REQUEST);
    }

    public static ErrorMessage parameterValidationProblem() {
        return new ErrorMessage(ErrorKey.PARAMETER_VALIDATION_PROBLEM, "There was an error while validating the parameters for the request.", HttpStatus.BAD_REQUEST);
    }

    public static ErrorMessage couldNotAssembleChunks() {
        return new ErrorMessage(ErrorKey.COULD_NOT_ASSEMBLE_CHUNKS, "Could not assemble the chunks for the message, the chunk context ID is invalid or there was a problem while merging the chunks.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage teamSetContextIdAlreadyInUse(String teamSetContextId) {
        return new ErrorMessage(ErrorKey.TEAM_SET_CONTEXT_ID_ALREADY_IN_USE, String.format("The team set context ID '%s' is already in use.", teamSetContextId), HttpStatus.CONFLICT);
    }

    public static ErrorMessage couldNotCreatePrivateKeyForApplication(String applicationId, String versionId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_CREATE_PRIVATE_KEY_FOR_APPLICATION, String.format("Could not create private key for application with id '%s' and version '%s'.", applicationId, versionId), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage couldNotCreatePublicKeyForApplication(String applicationId, String versionId) {
        return new ErrorMessage(ErrorKey.COULD_NOT_CREATE_PUBLIC_KEY_FOR_APPLICATION, String.format("Could not create public key for application with id '%s' and version '%s'.", applicationId, versionId), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorMessage notAuthorized() {
        return new ErrorMessage(ErrorKey.NOT_AUTHORIZED, "Nice try, but the user is not authorized to perform this action.", HttpStatus.FORBIDDEN);
    }

    public static ErrorMessage missingRouterDevice(String externalEndpointId) {
        return new ErrorMessage(ErrorKey.MISSING_ROUTER_DEVICE, String.format("Could not find the router device for the endpoint with the external endpoint ID '%s'.", externalEndpointId), HttpStatus.SERVICE_UNAVAILABLE);
    }

    public static ErrorMessage couldNotPublishHealthMessageSinceClientIsNotConnected() {
        return new ErrorMessage(ErrorKey.COULD_NOT_PUBLISH_HEALTH_MESSAGE, "Could not publish the health check message. MQTT client is not connected.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public static ErrorMessage couldNotDecodeBase64EncodedMessageContent() {
        return new ErrorMessage(ErrorKey.COULD_NOT_DECODE_BASE64_ENCODED_MESSAGE_CONTENT, "Could not decode the base64 encoded message content.", HttpStatus.BAD_REQUEST);
    }

    public static ErrorMessage routerDeviceAlreadyExists(String clientId) {
        return new ErrorMessage(ErrorKey.ROUTER_DEVICE_ALREADY_EXISTS, String.format("The router device with the client ID '%s' does already exist.", clientId), HttpStatus.CONFLICT);
    }

    public static ErrorMessage unknownError(String message) {
        return new ErrorMessage(ErrorKey.UNKNOWN_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}