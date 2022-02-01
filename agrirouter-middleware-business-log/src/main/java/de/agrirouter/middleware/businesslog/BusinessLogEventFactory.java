package de.agrirouter.middleware.businesslog;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.service.parameters.SetSubscriptionParameters;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.SupportedTechnicalMessageType;
import de.agrirouter.middleware.domain.Tenant;
import de.agrirouter.middleware.domain.enums.BusinessLogEventType;
import de.agrirouter.middleware.domain.log.BusinessLogEvent;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a business log event.
 */
@Service
public class BusinessLogEventFactory {

    /**
     * An application was saved.
     *
     * @param application -
     * @return -
     */
    public BusinessLogEvent applicationSaved(Application application) {
        return new BusinessLogEventBuilder()
                .application(application)
                .type(BusinessLogEventType.APPLICATION_CREATED)
                .message("The application version '%s' for the application with the id '%s' was created.", application.getApplicationId(), application.getVersionId())
                .build();
    }

    /**
     * Technical message types are updates.
     *
     * @param application                    -
     * @param supportedTechnicalMessageTypes -
     * @return -
     */
    public BusinessLogEvent technicalMessageTypesUpdated(Application application, Set<SupportedTechnicalMessageType> supportedTechnicalMessageTypes) {
        return new BusinessLogEventBuilder()
                .application(application)
                .type(BusinessLogEventType.TECHNICAL_MESSAGE_TYPES_UPDATED)
                .message("The technical messages types for the application version '%s' for the application with the id '%s' are updated to the following value '%s'.",
                        application.getApplicationId(),
                        application.getVersionId(),
                        supportedTechnicalMessageTypes
                                .stream()
                                .map(supportedTechnicalMessageType -> MessageFormat.format("[%s,%s]", supportedTechnicalMessageType.getTechnicalMessageType(), supportedTechnicalMessageType.getDirection()))
                                .collect(Collectors.joining(",")))
                .build();
    }

    /**
     * The endpoint was deactivated.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent endpointDeactivated(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.ENDPOINT_DEACTIVATED)
                .message("The endpoint '%s' was deactivated.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * The endpoint was activated.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent endpointActivated(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.ENDPOINT_ACTIVATED)
                .message("The endpoint '%s' was activated.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * A unknown message arrived.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent unknownMessageArrived(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.UNKNOWN_MESSAGE)
                .message("The unknown message arrived.")
                .build();
    }

    /**
     * A tenant was created.
     *
     * @param tenant -
     * @return -
     */
    public BusinessLogEvent tenantCreated(Tenant tenant) {
        return new BusinessLogEventBuilder()
                .type(BusinessLogEventType.TENANT_CREATED)
                .message("The tenant '%s' with the ID '%s' was created.", tenant.getName(), tenant.getTenantId())
                .build();
    }

    /**
     * Resend all capabilities for an application.
     *
     * @param application -
     * @return -
     */
    public BusinessLogEvent resendCapabilities(Application application) {
        return new BusinessLogEventBuilder()
                .application(application)
                .type(BusinessLogEventType.RESEND_CAPABILITIES)
                .message("The capabilities for the application version '%s' for the application with the id '%s' are resend.", application.getApplicationId(), application.getVersionId())
                .build();
    }


    /**
     * Resend all capabilities for an endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent resendCapabilities(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.RESEND_CAPABILITIES)
                .message("The capabilities for the endpoint are resend.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Resending the subscriptions.
     *
     * @param endpoint      -
     * @param subscriptions -
     * @return -
     */
    public BusinessLogEvent sendSubscriptions(Endpoint endpoint, List<SetSubscriptionParameters.Subscription> subscriptions) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.SEND_SUBSCRIPTIONS)
                .message("The following subscriptions [%s] for the endpoint with the id '%s' are send.", subscriptions
                        .stream()
                        .filter(subscription -> null != subscription.getTechnicalMessageType())
                        .map(subscription -> String.format("{%s}", Objects.requireNonNull(subscription.getTechnicalMessageType()).getKey()))
                        .collect(Collectors.joining(",")), endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Sending the capabilities of an endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent sendCapabilities(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.SEND_CAPABILITIES)
                .message("The capabilities for the endpoint with the id '%s' are send.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Fetching and confirming all messages for the endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent fetchAndConfirmExistingMessages(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.FETCH_AND_CONFIRM_MESSAGE)
                .message("Fetching and confirming all messages for the endpoint with the id '%s'.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Confirm message.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent confirmMessages(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.CONFIRM_MESSAGE)
                .message("Confirming messages for the endpoint with the id '%s'.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Delete message.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent deleteMessages(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.DELETE_MESSAGE)
                .message("Deleting messages for the endpoint with the id '%s'.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Persisting a content message.
     *
     * @param endpoint             -
     * @param technicalMessageType -
     * @return -
     */
    public BusinessLogEvent persistContentMessage(Endpoint endpoint, String technicalMessageType) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.PERSIST_CONTENT_MESSAGE)
                .message("Persisting a content message of the type '%s' for the endpoint with the id '%s'.", technicalMessageType, endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Persisting a content message in the document storage.
     *
     * @param endpoint             -
     * @param technicalMessageType -
     * @return -
     */
    public BusinessLogEvent persistContentMessageInDocumentStorage(Endpoint endpoint, String technicalMessageType) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.PERSIST_CONTENT_MESSAGE)
                .message("Persisting a content message of the type '%s' for the endpoint with the id '%s' in the document storage.", technicalMessageType, endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Onboard process for an existing endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent onboardEndpointAgain(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.ONBOARD_ENDPOINT_AGAIN)
                .message("The endpoint with the id '%s' was onboarded again.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Onboard process for a new endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent onboardEndpoint(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.ONBOARD_ENDPOINT)
                .message("The endpoint with the id '%s' was onboarded for the first time.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Onboard process for a new endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public BusinessLogEvent onboardVirtualEndpoint(Endpoint endpoint) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.ONBOARD_VIRTUAL_ENDPOINT)
                .message("A new virtual endpoint for the endpoint with the id '%s' was onboarded.", endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * Publish message for an endpoint.
     *
     * @param endpoint             -
     * @param technicalMessageType -
     * @return -
     */
    public BusinessLogEvent publishMessage(Endpoint endpoint, TechnicalMessageType technicalMessageType) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.PUBLISH_MESSAGE)
                .message("A message of the type '%s' for the endpoint with the id '%s' has been published.", technicalMessageType, endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * A device has been created.
     *
     * @param endpoint         -
     * @param manufacturerCode -
     * @param serialNumber     -
     * @return -
     */
    public BusinessLogEvent deviceCreated(Endpoint endpoint, int manufacturerCode, String serialNumber) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.DEVICE_CREATED)
                .message("The device with the manufacturer code '%s' and serial number '%s' was created for the endpoint with the ID '%s'.", manufacturerCode, serialNumber, endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * A device has been updated.
     *
     * @param endpoint         -
     * @param manufacturerCode -
     * @param serialNumber     -
     * @return -
     */
    public BusinessLogEvent deviceUpdated(Endpoint endpoint, int manufacturerCode, String serialNumber) {
        return new BusinessLogEventBuilder()
                .endpoint(endpoint)
                .type(BusinessLogEventType.DEVICE_CREATED)
                .message("The device with the manufacturer code '%s' and serial number '%s' was updated for the endpoint with the ID '%s'.", manufacturerCode, serialNumber, endpoint.getExternalEndpointId())
                .build();
    }

    /**
     * A device has been activated.
     *
     * @param teamSetContextId -
     * @return -
     */
    public BusinessLogEvent deviceActivated(String teamSetContextId) {
        return new BusinessLogEventBuilder()
                .type(BusinessLogEventType.DEVICE_ACTIVATED)
                .message("The device with the team set context ID '%s' was activated.", teamSetContextId)
                .build();
    }

    /**
     * A router device has been added.
     *
     * @param application -
     */
    public BusinessLogEvent routerDeviceAdded(Application application) {
        return new BusinessLogEventBuilder()
                .type(BusinessLogEventType.ROUTER_DEVICE_ADDED)
                .message("A router device has been added for the application '%s'.", application.getInternalApplicationId())
                .build();
    }
}
