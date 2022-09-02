package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.TechnicalConnectionState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Optional;

/**
 * The custom home controller.
 */
@Controller
public class EndpointDashboardUIController {

    private final EndpointService endpointService;
    private final MqttClientManagementService mqttClientManagementService;
    private final ApplicationService applicationService;

    public EndpointDashboardUIController(EndpointService endpointService,
                                         MqttClientManagementService mqttClientManagementService,
                                         ApplicationService applicationService) {
        this.endpointService = endpointService;
        this.mqttClientManagementService = mqttClientManagementService;
        this.applicationService = applicationService;
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
                model.addAttribute("warnings", endpointService.getWarnings(endpoint));
                model.addAttribute("errors", endpointService.getErrors(endpoint));
                TechnicalConnectionState technicalConnectionState = mqttClientManagementService.getTechnicalState(application, endpoint.asOnboardingResponse());
                model.addAttribute("technicalConnectionState", technicalConnectionState);
                model.addAttribute("connectionErrors", technicalConnectionState.connectionErrors());
            } else {
                return Routes.UI.ERROR;
            }
        } else {
            return Routes.UI.ERROR;
        }
        return Routes.UI.ENDPOINT_DASHBOARD;
    }

}
