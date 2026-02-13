package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.EndpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointMigrationService {

    private final ApplicationRepository applicationRepository;
    private final EndpointRepository endpointRepository;
    private final EndpointService endpointService;
    private final BusinessOperationLogService businessOperationLogService;

    @Transactional
    public void migrate(Principal principal, String externalEndpointId, String sourceInternalApplicationId, String targetInternalApplicationId) {
        if (StringUtils.equals(sourceInternalApplicationId, targetInternalApplicationId)) {
            throw new BusinessException(ErrorMessageFactory.invalidParameterForAction("sourceInternalApplicationId", "targetInternalApplicationId"));
        }
        final var endpointOpt = endpointRepository.findByExternalEndpointId(externalEndpointId);
        if (endpointOpt.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotFindEndpointByExternalId(externalEndpointId));
        } else {
            var endpoint = endpointOpt.get();

            var sourceApp = applicationRepository.findByInternalApplicationIdAndTenantId(sourceInternalApplicationId, principal.getName())
                    .orElseThrow(() -> new BusinessException(ErrorMessageFactory.couldNotFindApplication()));
            var targetApp = applicationRepository.findByInternalApplicationIdAndTenantId(targetInternalApplicationId, principal.getName())
                    .orElseThrow(() -> new BusinessException(ErrorMessageFactory.couldNotFindApplication()));

            // Check if endpoint belongs to source application
            if (!StringUtils.equals(endpoint.getApplicationId(), sourceApp.getId())) {
                throw new BusinessException(ErrorMessageFactory.invalidParameterForAction("sourceInternalApplicationId"));
            }

            // Update endpoint to belong to target application
            endpoint.setApplicationId(targetApp.getId());

            // Update application endpoint ID lists
            if (sourceApp.getEndpointIds() != null) {
                sourceApp.getEndpointIds().remove(endpoint.getId());
                applicationRepository.save(sourceApp);
            }

            if (targetApp.getEndpointIds() == null) {
                targetApp.setEndpointIds(new HashSet<>());
            }
            targetApp.getEndpointIds().add(endpoint.getId());
            applicationRepository.save(targetApp);

            var originalOnboardResponse = endpoint.asOnboardingResponse(true);
            var routerOnboardJson = targetApp.createOnboardResponseForRouterDevice(originalOnboardResponse);
            // Only update router device configuration if targetApp uses a router device
            if (targetApp.usesRouterDevice()) {
                endpoint.setOnboardResponseForRouterDevice(routerOnboardJson);
            } else {
                endpoint.setOnboardResponseForRouterDevice(null);
            }
            endpointRepository.save(endpoint);

            endpointService.sendCapabilities(targetApp, endpoint);

            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()),
                    "Endpoint migrated from application '%s' to '%s'.", sourceApp.getInternalApplicationId(), targetApp.getInternalApplicationId());
        }
    }

}
