package de.agrirouter.middleware.integration.status;

import com.google.gson.Gson;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.CriticalBusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.integration.status.dto.AgrirouterStatusResponse;
import de.agrirouter.middleware.integration.status.dto.Component;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

/**
 * Live status information for the agrirouter©.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgrirouterStatusIntegrationService {

    @Value("${app.agrirouter.status.url}")
    private String url;

    @Value("${app.agrirouter.status.skip-check}")
    private boolean skipStatusCheck;

    private Component latestStatus;

    private final Gson gson;

    @PostConstruct
    public void init() {
        latestStatus = fetchCurrentStatus();
    }

    protected Component fetchCurrentStatus() {
        var restTemplate = new RestTemplate();
        var response = restTemplate.getForObject(url, String.class);
        var agrirouterStatusResponse = gson.fromJson(response, AgrirouterStatusResponse.class);
        List<Component> components = Objects.requireNonNull(agrirouterStatusResponse)
                .getComponents();
        if (components != null && !components.isEmpty()) {
            components
                    .stream()
                    .filter(component -> component.getName().equals("agrirouter"))
                    .findFirst()
                    .ifPresent(component -> latestStatus = component);
            log.debug("Fetched status: {}", latestStatus.getComponentStatus());
        }
        return latestStatus;
    }

    /**
     * Scheduled task to fetch the current status.
     */
    @Scheduled(cron = "${app.scheduled.statuspage-check-interval}")
    protected void checkStatus() {
        latestStatus = fetchCurrentStatus();
    }

    /**
     * Check if the agrirouter© is available.
     */
    public void checkCurrentStatus() throws CriticalBusinessException {
        if (latestStatus == null || latestStatus.getComponentStatus() == null) {
            throw new BusinessException(ErrorMessageFactory.agrirouterStatusNotAvailable());
        } else {
            if (!latestStatus.getComponentStatus().isOperational()) {
                log.warn("Agrirouter status: {}", latestStatus.getComponentStatus());
                throw new CriticalBusinessException(ErrorMessageFactory.agrirouterStatusNotOperational());
            }
        }
    }

    /**
     * Check whether the agrirouter© is available.
     *
     * @return - true if the agrirouter© is available.
     */
    public boolean isOperational() {
        if (skipStatusCheck) {
            log.debug("Skipping status check, should only be used for development or temporary purposes.");
            return true;
        } else
            return latestStatus != null && latestStatus.getComponentStatus() != null && latestStatus.getComponentStatus().isOperational();
    }
}
