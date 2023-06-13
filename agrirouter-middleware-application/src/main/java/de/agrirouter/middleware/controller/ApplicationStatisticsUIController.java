package de.agrirouter.middleware.controller;

import de.agrirouter.middleware.api.Routes;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.business.TenantService;
import de.agrirouter.middleware.controller.dto.ApplicationStatisticsResponse;
import de.agrirouter.middleware.controller.dto.MqttStatisticsResponse;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.MqttStatistics;
import de.agrirouter.middleware.integration.status.AgrirouterStatusIntegrationService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * The custom application statistics controller.
 */
@Controller
public class ApplicationStatisticsUIController extends UIController {

    private final MqttStatistics mqttStatistics;
    private final MqttClientManagementService mqttClientManagementService;
    private final EndpointService endpointService;
    private final ApplicationService applicationService;
    private final TenantService tenantService;
    private final ModelMapper modelMapper;
    private final AgrirouterStatusIntegrationService agrirouterStatusIntegrationService;

    public ApplicationStatisticsUIController(MqttStatistics mqttStatistics,
                                             MqttClientManagementService mqttClientManagementService,
                                             EndpointService endpointService,
                                             ApplicationService applicationService,
                                             TenantService tenantService,
                                             ModelMapper modelMapper,
                                             AgrirouterStatusIntegrationService agrirouterStatusIntegrationService) {
        this.mqttStatistics = mqttStatistics;
        this.mqttClientManagementService = mqttClientManagementService;
        this.endpointService = endpointService;
        this.applicationService = applicationService;
        this.tenantService = tenantService;
        this.modelMapper = modelMapper;
        this.agrirouterStatusIntegrationService = agrirouterStatusIntegrationService;
    }

    /**
     * The home / landing page.
     *
     * @return -
     */
    @SuppressWarnings("unused")
    @GetMapping("/application-statistics")
    public String navigation(Principal principal, Model model) {
        addApplicationStatisticsToModel(model);
        addMqttStatisticsToModel(model);
        model.addAttribute("activeProfiles", getActiveProfiles());
        model.addAttribute("agrirouterStatus", agrirouterStatusIntegrationService.isOperational());
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

    private void addMqttStatisticsToModel(Model model) {
        var mqttStatisticsResponse = modelMapper.map(mqttStatistics, MqttStatisticsResponse.class);
        model.addAttribute("mqttStatistics", mqttStatisticsResponse);
    }

}
