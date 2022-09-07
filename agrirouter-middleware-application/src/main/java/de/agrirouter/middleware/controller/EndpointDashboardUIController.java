package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.cache.cloud.CloudOnboardingFailureCache;
import de.agrirouter.middleware.controller.dto.response.domain.MessageWaitingForAcknowledgementDto;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.log.Error;
import de.agrirouter.middleware.domain.log.Warning;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.TechnicalConnectionState;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The custom home controller.
 */
@Controller
public class EndpointDashboardUIController {

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
        Optional<Endpoint> optionalEndpoint = endpointService.findByExternalEndpointId(externalEndpointId);
        if (optionalEndpoint.isPresent()) {
            Endpoint endpoint = optionalEndpoint.get();
            Optional<Application> optionalApplication = applicationService.findByEndpoint(endpoint);
            if (optionalApplication.isPresent()) {
                Application application = optionalApplication.get();
                model.addAttribute("endpoint", endpoint);

                model.addAttribute("agrirouterApplication", application);

                List<Warning> warnings = endpointService.getWarnings(endpoint);
                warnings.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                model.addAttribute("warnings", warnings);

                List<Error> errors = endpointService.getErrors(endpoint);
                errors.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                model.addAttribute("errors", errors);

                TechnicalConnectionState technicalConnectionState = mqttClientManagementService.getTechnicalState(application, endpoint.asOnboardingResponse());
                model.addAttribute("technicalConnectionState", technicalConnectionState);

                model.addAttribute("connectionErrors", technicalConnectionState.connectionErrors());

                model.addAttribute("cloudOnboardingFailures", cloudOnboardingFailureCache.getAll(endpoint.getExternalEndpointId()));

                final var messagesWaitingForAcknowledgement = messageWaitingForAcknowledgementService.findAllForAgrirouterEndpointId(endpoint.getAgrirouterEndpointId())
                        .stream()
                        .map(messageWaitingForAcknowledgement -> modelMapper.map(messageWaitingForAcknowledgement, MessageWaitingForAcknowledgementDto.class))
                        .peek(messageWaitingForAcknowledgementDto -> messageWaitingForAcknowledgementDto.setHumanReadableCreated(Date.from(Instant.ofEpochSecond(messageWaitingForAcknowledgementDto.getCreated())))).toList();
                model.addAttribute("messagesWaitingForAcknowledgement", messagesWaitingForAcknowledgement);

            } else {
                return Routes.UI.ERROR;
            }
        } else {
            return Routes.UI.ERROR;
        }
        return Routes.UI.ENDPOINT_DASHBOARD;
    }

}
