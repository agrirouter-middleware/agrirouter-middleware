package de.agrirouter.middleware.business.listener;

import com.dke.data.agrirouter.api.dto.onboard.OnboardingResponse;
import com.dke.data.agrirouter.api.enums.SystemMessageType;
import com.dke.data.agrirouter.api.service.parameters.CloudOffboardingParameters;
import com.dke.data.agrirouter.convenience.decode.DecodeCloudOnboardingResponsesService;
import com.dke.data.agrirouter.impl.messaging.mqtt.CloudOffboardingServiceImpl;
import com.google.gson.Gson;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.CloudRegistrationEvent;
import de.agrirouter.middleware.api.events.EndpointStatusUpdateEvent;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.business.DeviceDescriptionService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.enums.EndpointType;
import de.agrirouter.middleware.integration.EndpointIntegrationService;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgement;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.container.VirtualEndpointOnboardStateContainer;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.parameters.VirtualOffboardProcessIntegrationParameters;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Integration service to handle the onboard requests.
 */
@Slf4j
@Service
public class CloudRegistrationEventListener {

    private final ApplicationRepository applicationRepository;
    private final EndpointRepository endpointRepository;
    private final EndpointIntegrationService endpointIntegrationService;
    private final MqttClientManagementService mqttClientManagementService;
    private final DecodeCloudOnboardingResponsesService decodeCloudOnboardingResponsesService;
    private final EndpointService endpointService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final VirtualEndpointOnboardStateContainer virtualEndpointOnboardStateContainer;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BusinessOperationLogService businessOperationLogService;

    private final DeviceDescriptionService deviceDescriptionService;

    public CloudRegistrationEventListener(ApplicationRepository applicationRepository,
                                          EndpointRepository endpointRepository,
                                          EndpointIntegrationService endpointIntegrationService,
                                          MqttClientManagementService mqttClientManagementService,
                                          DecodeCloudOnboardingResponsesService decodeCloudOnboardingResponsesService,
                                          EndpointService endpointService,
                                          MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                          VirtualEndpointOnboardStateContainer virtualEndpointOnboardStateContainer,
                                          ApplicationEventPublisher applicationEventPublisher,
                                          BusinessOperationLogService businessOperationLogService,
                                          DeviceDescriptionService deviceDescriptionService) {
        this.applicationRepository = applicationRepository;
        this.endpointRepository = endpointRepository;
        this.endpointIntegrationService = endpointIntegrationService;
        this.mqttClientManagementService = mqttClientManagementService;
        this.decodeCloudOnboardingResponsesService = decodeCloudOnboardingResponsesService;
        this.endpointService = endpointService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.virtualEndpointOnboardStateContainer = virtualEndpointOnboardStateContainer;
        this.applicationEventPublisher = applicationEventPublisher;
        this.businessOperationLogService = businessOperationLogService;
        this.deviceDescriptionService = deviceDescriptionService;
    }

    /**
     * Virtual endpoint offboard process.
     *
     * @param virtualOffboardProcessIntegrationParameters The parameters for the offboard process.
     */
    private void offboard(VirtualOffboardProcessIntegrationParameters virtualOffboardProcessIntegrationParameters) {
        virtualOffboardProcessIntegrationParameters.getEndpointIds().forEach(this::checkWhetherTheEndpointIsVirtualOrNot);
        final var endpoint = virtualOffboardProcessIntegrationParameters.getEndpoint();
        final var onboardingResponse = endpoint.asOnboardingResponse();
        final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
        if (iMqttClient.isEmpty()) {
            log.error(ErrorMessageFactory.couldNotConnectMqttClient(onboardingResponse.getSensorAlternateId()).asLogMessage());
        } else {
            final var cloudOffboardingService = new CloudOffboardingServiceImpl(iMqttClient.get());
            final var parameters = new CloudOffboardingParameters();
            parameters.setOnboardingResponse(onboardingResponse);
            parameters.setEndpointIds(getEndpointIds(virtualOffboardProcessIntegrationParameters));

            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Offboarding the following endpoints {}", String.join(" ,", Objects.requireNonNull(parameters.getEndpointIds())));
            final var messageId = cloudOffboardingService.send(parameters);

            log.debug("Saving message with ID '{}'  waiting for ACK.", messageId);
            MessageWaitingForAcknowledgement messageWaitingForAcknowledgement = new MessageWaitingForAcknowledgement();
            messageWaitingForAcknowledgement.setAgrirouterEndpointId(onboardingResponse.getSensorAlternateId());
            messageWaitingForAcknowledgement.setMessageId(messageId);
            messageWaitingForAcknowledgement.setTechnicalMessageType(SystemMessageType.DKE_CLOUD_OFFBOARD_ENDPOINTS.getKey());
            messageWaitingForAcknowledgementService.save(messageWaitingForAcknowledgement);

            virtualOffboardProcessIntegrationParameters.getEndpointIds().forEach(endpointService::deleteEndpointData);
        }
    }

    private List<String> getEndpointIds(VirtualOffboardProcessIntegrationParameters virtualOffboardProcessIntegrationParameters) {
        final var agrirouterEndpointIds = new ArrayList<String>();
        virtualOffboardProcessIntegrationParameters.getEndpointIds().forEach(s -> {
            final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndIgnoreDisabled(s);
            optionalEndpoint.ifPresent(endpoint -> agrirouterEndpointIds.add(endpoint.getAgrirouterEndpointId()));
        });
        agrirouterEndpointIds.addAll(virtualOffboardProcessIntegrationParameters.getEndpointIds());
        return agrirouterEndpointIds;
    }

    private void checkWhetherTheEndpointIsVirtualOrNot(String endpointId) {
        final var optionalEndpoint = endpointRepository.findByExternalEndpointIdAndEndpointType(endpointId, EndpointType.VIRTUAL);
        if (optionalEndpoint.isEmpty()) {
            log.error(ErrorMessageFactory.couldNotFindVirtualEndpoint().asLogMessage());
        }
    }

    /**
     * Handle cloud registration feedback from the AR.
     *
     * @param cloudRegistrationEvent -
     */
    @EventListener
    @Transactional
    public void onboardVirtualEndpoint(CloudRegistrationEvent cloudRegistrationEvent) {
        log.debug("Incoming event for cloud registration.");
        final var fetchMessageResponse = cloudRegistrationEvent.getFetchMessageResponse();
        log.debug("Find the corresponding endpoint with the ID '{}' for the cloud registration.", fetchMessageResponse.getSensorAlternateId());
        final var optionalEndpoint = endpointRepository.findByAgrirouterEndpointId(fetchMessageResponse.getSensorAlternateId());
        if (optionalEndpoint.isPresent() && !optionalEndpoint.get().isDeactivated()) {
            final var endpoint = optionalEndpoint.get();
            final var optionalApplication = applicationRepository.findByEndpointsContains(endpoint);
            if (optionalApplication.isPresent()) {
                final var application = optionalApplication.get();
                final var optionalOnboardState = virtualEndpointOnboardStateContainer.pop(cloudRegistrationEvent.getApplicationMessageId());
                if (optionalOnboardState.isPresent()) {
                    final var onboardState = optionalOnboardState.get();
                    final var cloudOnboardResponses = decodeCloudOnboardingResponsesService.decode(Collections.singletonList(fetchMessageResponse), endpoint.asOnboardingResponse());
                    cloudOnboardResponses.forEach(cloudOnboardResponse -> {
                        businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Onboard process of the following virtual endpoint was successful >>> {}", cloudOnboardResponse.getSensorAlternateId());
                        log.debug("Saving the following cloud onboard response to the database >>> {}", cloudOnboardResponse);
                        var virtualEndpoint = new Endpoint();
                        virtualEndpoint.setAgrirouterEndpointId(cloudOnboardResponse.getSensorAlternateId());
                        virtualEndpoint.setExternalEndpointId(onboardState.getExternalEndpointId());
                        virtualEndpoint.setOnboardResponse(new Gson().toJson(cloudOnboardResponse));
                        virtualEndpoint.setOnboardResponseForRouterDevice(application.createOnboardResponseForRouterDevice(virtualEndpoint.asOnboardingResponse(true)));
                        virtualEndpoint.setEndpointType(EndpointType.VIRTUAL);
                        virtualEndpoint = endpointRepository.save(virtualEndpoint);
                        endpointRepository.save(virtualEndpoint);
                        endpoint.getConnectedVirtualEndpoints().add(virtualEndpoint);
                        endpointRepository.save(endpoint);
                        application.getEndpoints().add(virtualEndpoint);
                        applicationRepository.save(application);
                        endpointIntegrationService.sendCapabilities(application, virtualEndpoint);
                        deviceDescriptionService.checkAndSendCachedDeviceDescription(virtualEndpoint.getExternalEndpointId());
                        applicationEventPublisher.publishEvent(new EndpointStatusUpdateEvent(this, virtualEndpoint.getAgrirouterEndpointId(), null));
                    });
                } else {
                    log.warn("Since the state for the message ID '{}' has not been found the endpoints are removed from the AR to avoid problems.", cloudRegistrationEvent.getApplicationMessageId());
                    final var offboardVirtualEndpointParameters = new VirtualOffboardProcessIntegrationParameters();
                    offboardVirtualEndpointParameters.setEndpoint(endpoint);
                    final var cloudOnboardResponses = decodeCloudOnboardingResponsesService.decode(Collections.singletonList(fetchMessageResponse), endpoint.asOnboardingResponse());
                    final var endpointIds = cloudOnboardResponses.stream().map(OnboardingResponse::getSensorAlternateId).collect(Collectors.toList());
                    offboardVirtualEndpointParameters.setEndpointIds(endpointIds);
                    offboard(offboardVirtualEndpointParameters);
                }
            } else {
                log.error(ErrorMessageFactory.couldNotFindApplication().asLogMessage());
            }
        } else {
            log.error(ErrorMessageFactory.couldNotFindEndpoint().asLogMessage());
        }
    }
}
