package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.controller.dto.enums.OverallEndpointStatusForUi;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.domain.log.Error;
import de.agrirouter.middleware.domain.log.Warning;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

/**
 * The endpoints controller.
 */
@Controller
public class EndpointsController {

    private final EndpointService endpointService;
    private final MqttClientManagementService mqttClientManagementService;

    public EndpointsController(EndpointService endpointService, MqttClientManagementService mqttClientManagementService) {
        this.endpointService = endpointService;
        this.mqttClientManagementService = mqttClientManagementService;
    }

    /**
     * The home / landing page.
     *
     * @return -
     */
    @SuppressWarnings("unused")
    @GetMapping("/endpoints")
    public String navigation(Principal principal, @RequestParam(value = "internalApplicationId") String internalApplicationId, Model model) {
        List<Endpoint> endpoints = endpointService.findAll(internalApplicationId);
        model.addAttribute("endpoints", endpoints);
        return Routes.UI.ENDPOINTS;
    }

    /**
     * Determine the overall status of an endpoint.
     *
     * @param endpoint The endpoint.
     * @return The overall status.
     */
    @SuppressWarnings("unused")
    public OverallEndpointStatusForUi determineOverallStatus(Endpoint endpoint) {
        var metric = 100;
        final var toolTip = new StringBuilder();
        if (!endpoint.getEndpointStatus().getConnectionState().isConnected()) {
            metric -= 100;
            toolTip.append("The endpoint is not connected");
        } else {
            toolTip.append("The endpoint is connected. ");
            int pendingDeliveryTokens = mqttClientManagementService.getPendingDeliveryTokens(endpoint.asOnboardingResponse());
            if (pendingDeliveryTokens > 0) {
                metric -= 75;
                toolTip.append("There are ").append(pendingDeliveryTokens).append(" pending delivery tokens. ");
            } else {
                toolTip.append("There are no pending delivery tokens. ");
            }

            List<Error> errors = endpointService.getErrors(endpoint);
            if (errors.size() > 0) {
                toolTip.append("There are ").append(errors.size()).append(" errors. ");
                metric -= errors.size() * 5;
            } else {
                toolTip.append("There are no errors. ");
            }

            List<Warning> warnings = endpointService.getWarnings(endpoint);
            if (warnings.size() > 0) {
                toolTip.append("There are ").append(warnings.size()).append(" warnings. ");
                metric -= warnings.size() * 3;
            } else {
                toolTip.append("There are no warnings. ");
            }
        }
        return OverallEndpointStatusForUi.fromMetric(metric, toolTip.toString());
    }
}
