package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.cache.cloud.CloudOnboardingFailureCache;
import de.agrirouter.middleware.controller.dto.response.domain.MessageWaitingForAcknowledgementDto;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

/**
 * The custom home controller.
 */
@Controller
public class EndpointDashboardUIController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointDashboardUIController.class);

    private final EndpointService endpointService;
    private final MqttClientManagementService mqttClientManagementService;
    private final ApplicationService applicationService;
    private final MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;
    private final ModelMapper modelMapper;
    private final CloudOnboardingFailureCache cloudOnboardingFailureCache;

    public EndpointDashboardUIController(EndpointService endpointService,
                                         MqttClientManagementService mqttClientManagementService,
                                         ApplicationService applicationService,
                                         MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService,
                                         ModelMapper modelMapper,
                                         CloudOnboardingFailureCache cloudOnboardingFailureCache) {
        this.endpointService = endpointService;
        this.mqttClientManagementService = mqttClientManagementService;
        this.applicationService = applicationService;
        this.messageWaitingForAcknowledgementService = messageWaitingForAcknowledgementService;
        this.modelMapper = modelMapper;
        this.cloudOnboardingFailureCache = cloudOnboardingFailureCache;
    }

    /**
     * The landing page.
     *
     * @return -
     */
    @SuppressWarnings("unused")
    @GetMapping("/endpoint-dashboard")
    public String navigation(Principal principal, @RequestParam(value = "externalEndpointId") String externalEndpointId, Model model) {
        var optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            final var endpoint = optionalEndpoint.get();
            var optionalApplication = applicationService.findByEndpoint(endpoint);
            if (optionalApplication.isPresent()) {
                final var application = optionalApplication.get();
                model.addAttribute("endpoint", endpoint);

                model.addAttribute("agrirouterApplication", application);

                final var warnings = endpointService.getWarnings(endpoint);
                warnings.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                model.addAttribute("warnings", warnings);

                final var errors = endpointService.getErrors(endpoint);
                errors.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                model.addAttribute("errors", errors);

                final var technicalConnectionState = mqttClientManagementService.getTechnicalState(application, endpoint.asOnboardingResponse());
                model.addAttribute("technicalConnectionState", technicalConnectionState);

                model.addAttribute("connectionErrors", technicalConnectionState.connectionErrors());

                model.addAttribute("cloudOnboardingFailures", cloudOnboardingFailureCache.getAll(endpoint.getExternalEndpointId()));

                model.addAttribute("pendingDeliveryTokens", mqttClientManagementService.getPendingDeliveryTokens(endpoint.asOnboardingResponse()));

                final var messagesWaitingForAcknowledgement = new ArrayList<>(messageWaitingForAcknowledgementService.findAllForAgrirouterEndpointId(endpoint.getAgrirouterEndpointId())
                        .stream()
                        .map(messageWaitingForAcknowledgement -> modelMapper.map(messageWaitingForAcknowledgement, MessageWaitingForAcknowledgementDto.class))
                        .peek(messageWaitingForAcknowledgementDto -> messageWaitingForAcknowledgementDto.setHumanReadableCreated(Date.from(Instant.ofEpochSecond(messageWaitingForAcknowledgementDto.getCreated())))).toList());
                messagesWaitingForAcknowledgement.sort((o1, o2) -> Long.compare(o2.getCreated(), o1.getCreated()));
                model.addAttribute("messagesWaitingForAcknowledgement", messagesWaitingForAcknowledgement);

            } else {
                return Routes.UI.ERROR;
            }
        } else {
            return Routes.UI.ERROR;
        }
        return Routes.UI.ENDPOINT_DASHBOARD;
    }

    /**
     * Clearing errors.
     *
     * @return -
     */
    @PostMapping("/endpoint-dashboard/clear-errors")
    public String clearErrors(@RequestParam(value = "externalEndpointId") String externalEndpointId) {
        LOGGER.debug("Clearing errors for endpoint with external endpoint ID {}.", externalEndpointId);
        endpointService.resetErrors(externalEndpointId);
        return "redirect:/endpoint-dashboard?externalEndpointId=" + externalEndpointId;
    }

    /**
     * Clearing warnings.
     *
     * @return -
     */
    @PostMapping("/endpoint-dashboard/clear-warnings")
    public String clearWarnings(@RequestParam(value = "externalEndpointId") String externalEndpointId) {
        LOGGER.debug("Clearing warnings for endpoint with external endpoint ID {}.", externalEndpointId);
        endpointService.resetWarnings(externalEndpointId);
        return "redirect:/endpoint-dashboard?externalEndpointId=" + externalEndpointId;
    }

    /**
     * Clearing messages waiting for ACK.
     *
     * @return -
     */
    @PostMapping("/endpoint-dashboard/clear-messages-waiting-for-ack")
    public String clearMessagesWaitingForAck(@RequestParam(value = "externalEndpointId") String externalEndpointId) {
        LOGGER.debug("Clearing messages waiting for ACK for endpoint with external endpoint ID {}.", externalEndpointId);
        endpointService.resetMessagesWaitingForAcknowledgement(externalEndpointId);
        return "redirect:/endpoint-dashboard?externalEndpointId=" + externalEndpointId;
    }

    /**
     * Clearing connection errors.
     *
     * @return -
     */
    @PostMapping("/endpoint-dashboard/clear-connection-errors")
    public String clearConnectionErrors(@RequestParam(value = "externalEndpointId") String externalEndpointId) {
        LOGGER.debug("Clearing connection errors for the endpoint with external endpoint ID {}.", externalEndpointId);
        endpointService.resetConnectionErrors(externalEndpointId);
        return "redirect:/endpoint-dashboard?externalEndpointId=" + externalEndpointId;
    }

}
