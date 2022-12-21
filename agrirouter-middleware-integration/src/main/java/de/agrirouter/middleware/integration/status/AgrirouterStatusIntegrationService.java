package de.agrirouter.middleware.integration.status;

import com.google.gson.Gson;
import de.agrirouter.middleware.integration.status.dto.AgrirouterStatusResponse;
import de.agrirouter.middleware.integration.status.dto.Component;
import de.agrirouter.middleware.integration.status.dto.ComponentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Live status information for the agrirouter©.
 */
@Slf4j
@Service
public class AgrirouterStatusIntegrationService {

    @Value("${app.agrirouter.status.url}")
    private String url;

    private Component latestStatus;

    @PostConstruct
    public void init() {
        latestStatus = fetchCurrentStatus();
    }

    protected Component fetchCurrentStatus() {
        var restTemplate = new RestTemplate();
        var response = restTemplate.getForObject(url, String.class);
        AgrirouterStatusResponse agrirouterStatusResponse = new Gson().fromJson(response, AgrirouterStatusResponse.class);
        List<Component> components = Objects.requireNonNull(agrirouterStatusResponse)
                .getComponents();
        if (components != null && !components.isEmpty()) {
            components
                    .stream()
                    .filter(component -> component.getName().equals("agrirouter"))
                    .findFirst()
                    .ifPresent(component -> latestStatus = component);
            log.trace("Fetched status: {}", latestStatus);
            log.debug("Fetched status: {}", latestStatus.getComponentStatus());
        }
        return latestStatus;
    }

    /**
     * Check if the agrirouter© is operational.
     *
     * @return - true if operational, false otherwise.
     */
    public boolean isOperational() {
        return Optional.ofNullable(latestStatus)
                .map(Component::getComponentStatus)
                .map(ComponentStatus::isOperational)
                .orElse(false);
    }

    // Scheduled check for the status
    @Scheduled(cron = "${app.scheduled.statuspage-check-interval}")
    public void checkStatus() {
        latestStatus = fetchCurrentStatus();
    }

}
