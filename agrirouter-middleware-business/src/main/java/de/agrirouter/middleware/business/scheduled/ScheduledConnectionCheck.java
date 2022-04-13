package de.agrirouter.middleware.business.scheduled;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled fetching and confirming of messages.
 */
@Slf4j
@Component
public class ScheduledConnectionCheck {

    private final MqttClientManagementService mqttClientManagementService;
    private final BusinessOperationLogService businessOperationLogService;
    private final ApplicationRepository applicationRepository;
    private final EndpointRepository endpointRepository;

    public ScheduledConnectionCheck(MqttClientManagementService mqttClientManagementService,
                                    BusinessOperationLogService businessOperationLogService,
                                    ApplicationRepository applicationRepository,
                                    EndpointRepository endpointRepository) {
        this.mqttClientManagementService = mqttClientManagementService;
        this.businessOperationLogService = businessOperationLogService;
        this.applicationRepository = applicationRepository;
        this.endpointRepository = endpointRepository;
    }

    /**
     * Schedule the connection check in a cyclic manner.
     */
    @Scheduled(cron = "${app.scheduled.connection-check}")
    public void connectionCheck() {
        log.debug("Scheduled connection check.");
        applicationRepository.findAll().forEach(application -> application.getEndpoints().forEach(endpoint -> {
            log.debug("Activating endpoint {} for the connection check.", endpoint.getExternalEndpointId());
            endpoint.setDeactivated(false);
            try {
                final var onboardingResponse = endpoint.asOnboardingResponse();
                final var iMqttClient = mqttClientManagementService.get(onboardingResponse);
                if (iMqttClient.isEmpty()) {
                    log.warn("Deactivating endpoint {}.", endpoint.getExternalEndpointId());
                    log.warn("Scheduled connection check for endpoint '{}' has FAILED.", endpoint.getExternalEndpointId());
                    endpoint.setDeactivated(true);
                    if(!application.usesRouterDevice()) {
                        mqttClientManagementService.disconnect(onboardingResponse);
                    }
                } else {
                    final var mqttClient = iMqttClient.get();
                    if (mqttClient.isConnected()) {
                        mqttClient.unsubscribe(onboardingResponse.getConnectionCriteria().getCommands());
                        mqttClient.subscribe(onboardingResponse.getConnectionCriteria().getCommands());
                        log.debug("Scheduled connection check for MQTT client '{}' was successful.", mqttClient.getClientId());
                    } else {
                        log.warn("Deactivating endpoint {}.", endpoint.getExternalEndpointId());
                        log.warn("Scheduled connection check for MQTT client '{}' has FAILED.", mqttClient.getClientId());
                        endpoint.setDeactivated(true);
                        if(!application.usesRouterDevice()) {
                            mqttClientManagementService.disconnect(onboardingResponse);
                        }
                        log.warn("Scheduled connection check for MQTT client '{}' has FAILED.", mqttClient.getClientId());
                    }
                }
            } catch (BusinessException | MqttException e) {
                endpoint.setDeactivated(true);
                if(!application.usesRouterDevice()) {
                    mqttClientManagementService.disconnect(endpoint.asOnboardingResponse());
                }
                log.error("Could not perform connection check for the endpoint '{}' since there was an exception.", endpoint.getExternalEndpointId(), e);
            }
            if (endpoint.isDeactivated()) {
                log.warn("Removing connection for endpoint {}.", endpoint.getExternalEndpointId());
                if(!application.usesRouterDevice()) {
                    mqttClientManagementService.disconnect(endpoint.asOnboardingResponse());
                }
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Scheduled connection check was _NOT_ successful. The endpoint was deactivated to reduce error messages.");
            } else {
                businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()), "Scheduled connection check was successful.");
            }
            endpointRepository.save(endpoint);
        }));
    }
}
