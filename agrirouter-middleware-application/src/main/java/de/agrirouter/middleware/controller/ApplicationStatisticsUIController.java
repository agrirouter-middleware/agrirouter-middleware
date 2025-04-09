package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.TenantService;
import de.agrirouter.middleware.controller.dto.ApplicationStatisticsResponse;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * The custom application statistics controller.
 */
@Controller
@RequiredArgsConstructor
public class ApplicationStatisticsUIController extends UIController {

    private final MqttClientManagementService mqttClientManagementService;
    private final EndpointService endpointService;
    private final ApplicationService applicationService;
    private final TenantService tenantService;
    private final ModelMapper modelMapper;

    /**
     * The home / landing page.
     *
     * @return -
     */
    @SuppressWarnings("unused")
    @GetMapping("/application-statistics")
    public String navigation(Principal principal, Model model) {
        addApplicationStatisticsToModel(model);
        model.addAttribute("activeProfiles", getActiveProfiles());
        model.addAttribute("mqttConnectionStatus", mqttClientManagementService.getMqttConnectionStatus());
        return Routes.UserInterface.ThymeleafRouting.APPLICATION_STATISTICS;
    }

    private void addApplicationStatisticsToModel(Model model) {
        var applicationStatisticsResponse = ApplicationStatisticsResponse.builder()
                .nrOfApplications(applicationService.getNrOfApplications())
                .nrOfTenants(tenantService.getNrOfTenants())
                .nrOfEndpoints(endpointService.getNrOfEndpoints())
                .nrOfVirtualEndpoints(endpointService.getNrOfVirtualEndpoints())
                .nrOfConnectedClients(mqttClientManagementService.getNumberOfActiveConnections())
                .nrOfConnectedClients(mqttClientManagementService.getNumberOfActiveConnections())
                .nrOfDisconnectedClients(mqttClientManagementService.getNumberOfInactiveConnections())
                .build();
        model.addAttribute("applicationStatistics", applicationStatisticsResponse);
    }

}
