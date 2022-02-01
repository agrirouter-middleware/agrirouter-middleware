package de.agrirouter.middleware.businesslog;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.dke.data.agrirouter.api.service.parameters.SetSubscriptionParameters;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.SupportedTechnicalMessageType;
import de.agrirouter.middleware.domain.Tenant;
import de.agrirouter.middleware.domain.log.BusinessLogEvent;
import de.agrirouter.middleware.persistence.BusinessLogEventRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/**
 * Saving a business log to take care of the transactions within the middleware.
 */
@Service
public class BusinessLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessLogService.class);

    private final BusinessLogEventFactory businessLogEventFactory;
    private final EndpointRepository endpointRepository;
    private final BusinessLogEventRepository businessLogEventRepository;

    public BusinessLogService(BusinessLogEventFactory businessLogEventFactory,
                              EndpointRepository endpointRepository,
                              BusinessLogEventRepository businessLogEventRepository) {
        this.businessLogEventFactory = businessLogEventFactory;
        this.endpointRepository = endpointRepository;
        this.businessLogEventRepository = businessLogEventRepository;
    }

    /**
     * An application was saved.
     */
    @Async
    public void applicationSaved(Application application) {
        final var businessLogEvent = businessLogEventFactory.applicationSaved(application);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Technical message types are updates.
     *
     * @param application                    -
     * @param supportedTechnicalMessageTypes -
     */
    @Async
    public void technicalMessageTypesUpdated(Application application, Set<SupportedTechnicalMessageType> supportedTechnicalMessageTypes) {
        final var businessLogEvent = businessLogEventFactory.technicalMessageTypesUpdated(application, supportedTechnicalMessageTypes);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * The endpoint was deactivated.
     *
     * @param endpoint -
     */
    @Async
    public void endpointDeactivated(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.endpointDeactivated(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * The endpoint was activated.
     *
     * @param endpoint -
     */
    @Async
    public void endpointActivated(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.endpointActivated(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * An unknown message arrived for the endpoint.
     *
     * @param endpoint -
     */
    @Async
    public void unknownMessageArrived(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.unknownMessageArrived(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * A new tenant was created.
     *
     * @param tenant -
     */
    @Async
    public void tenantCreated(Tenant tenant) {
        final var businessLogEvent = businessLogEventFactory.tenantCreated(tenant);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Resending all capabilities for an application.
     *
     * @param application -
     */
    @Async
    public void resendCapabilities(Application application) {
        final var businessLogEvent = businessLogEventFactory.resendCapabilities(application);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Resending all capabilities for an endpoint.
     *
     * @param endpoint -
     */
    @Async
    public void resendCapabilities(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.resendCapabilities(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    private void handleBusinessLogEvent(BusinessLogEvent businessLogEvent) {
        LOGGER.debug(String.valueOf(businessLogEvent));
        businessLogEventRepository.save(businessLogEvent);
    }

    /**
     * Resending the subscriptions.
     *
     * @param endpoint      -
     * @param subscriptions -
     */
    @Async
    public void sendSubscriptions(Endpoint endpoint, List<SetSubscriptionParameters.Subscription> subscriptions) {
        final var businessLogEvent = businessLogEventFactory.sendSubscriptions(endpoint, subscriptions);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Sending capabilities.
     *
     * @param endpoint -
     */
    @Async
    public void sendCapabilities(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.sendCapabilities(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Fetching and confirming all existing messages.
     *
     * @param endpoint -
     */
    @Async
    public void fetchAndConfirmExistingMessages(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.fetchAndConfirmExistingMessages(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Confirm message.
     *
     * @param endpoint -
     */
    @Async
    public void confirmMessages(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.confirmMessages(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Confirm message.
     *
     * @param endpoint -
     */
    @Async
    public void deleteMessages(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.deleteMessages(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Persisting a content message.
     *
     * @param receiverId           -
     * @param technicalMessageType -
     */
    @Async
    public void persistContentMessage(String receiverId, String technicalMessageType) {
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(receiverId);
        if (optionalEndpoint.isPresent()) {
            final var businessLogEvent = businessLogEventFactory.persistContentMessage(optionalEndpoint.get(), technicalMessageType);
            handleBusinessLogEvent(businessLogEvent);
        } else {
            LOGGER.warn("Could not find endpoint for business log event.");
        }
    }

    /**
     * Persisting a content message.
     *
     * @param receiverId           -
     * @param technicalMessageType -
     */
    @Async
    public void persistContentMessageInDocumentStorage(String receiverId, String technicalMessageType) {
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(receiverId);
        if (optionalEndpoint.isPresent()) {
            final var businessLogEvent = businessLogEventFactory.persistContentMessageInDocumentStorage(optionalEndpoint.get(), technicalMessageType);
            handleBusinessLogEvent(businessLogEvent);
        } else {
            LOGGER.warn("Could not find endpoint for business log event.");
        }
    }

    /**
     * Onboard process for an existing endpoint.
     *
     * @param endpoint -
     */
    @Async
    public void onboardEndpointAgain(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.onboardEndpointAgain(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Onboard process for a new endpoint.
     *
     * @param endpoint -
     */
    @Async
    public void onboardEndpoint(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.onboardEndpoint(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * Onboard process for a virtual endpoint.
     *
     * @param endpoint -
     */
    @Async
    public void onboardVirtualEndpoint(Endpoint endpoint) {
        final var businessLogEvent = businessLogEventFactory.onboardVirtualEndpoint(endpoint);
        handleBusinessLogEvent(businessLogEvent);
    }


    /**
     * Cleaning the history once a day. All entries, older than 4 weeks are removed.
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 360_000)
    public void cleanLogHistory() {
        final var fourWeeks = Instant.now().minus(28, ChronoUnit.DAYS);
        businessLogEventRepository.deleteBusinessLogEventByVersionBefore(LocalDateTime.ofInstant(fourWeeks, ZoneOffset.UTC));
    }

    /**
     * Publish a message for the dedicated endpoint.
     *
     * @param endpoint             -
     * @param technicalMessageType -
     */
    public void publishMessage(Endpoint endpoint, TechnicalMessageType technicalMessageType) {
        final var businessLogEvent = businessLogEventFactory.publishMessage(endpoint, technicalMessageType);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * The device is created.
     *
     * @param manufacturerCode -
     * @param serialNumber     -
     */
    public void deviceCreated(Endpoint endpoint, int manufacturerCode, String serialNumber) {
        final var businessLogEvent = businessLogEventFactory.deviceCreated(endpoint, manufacturerCode, serialNumber);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * The device is updated.
     *
     * @param manufacturerCode -
     * @param serialNumber     -
     */
    public void deviceUpdated(Endpoint endpoint, int manufacturerCode, String serialNumber) {
        final var businessLogEvent = businessLogEventFactory.deviceUpdated(endpoint, manufacturerCode, serialNumber);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * The device is activated.
     *
     * @param teamSetContextId -
     */
    public void deviceActivated(String teamSetContextId) {
        final var businessLogEvent = businessLogEventFactory.deviceActivated(teamSetContextId);
        handleBusinessLogEvent(businessLogEvent);
    }

    /**
     * A router device has been added.
     *
     * @param application -
     */
    public void routerDeviceAdded(Application application) {
        final var businessLogEvent = businessLogEventFactory.routerDeviceAdded(application);
        handleBusinessLogEvent(businessLogEvent);
    }
}
