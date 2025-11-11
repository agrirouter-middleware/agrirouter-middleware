package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.api.logging.EndpointLogInformation;
import de.agrirouter.middleware.persistence.jpa.ApplicationRepository;
import de.agrirouter.middleware.persistence.jpa.EndpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashSet;
import java.util.Objects;

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
            throw new BusinessException(ErrorMessageFactory.unknownError(String.format("Could not find endpoint with the external endpoint ID '%s'.", externalEndpointId)));
        } else {
            var endpoint = endpointOpt.get();

            var sourceApp = applicationRepository.findByInternalApplicationIdAndTenantTenantId(sourceInternalApplicationId, principal.getName())
                    .orElseThrow(() -> new BusinessException(ErrorMessageFactory.couldNotFindApplication()));
            var targetApp = applicationRepository.findByInternalApplicationIdAndTenantTenantId(targetInternalApplicationId, principal.getName())
                    .orElseThrow(() -> new BusinessException(ErrorMessageFactory.couldNotFindApplication()));

            var ownerOfEndpointOpt = applicationRepository.findByEndpointsContains(endpoint);
            if (ownerOfEndpointOpt.isEmpty()) {
                throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
            }
            if (!Objects.equals(ownerOfEndpointOpt.get().getInternalApplicationId(), sourceApp.getInternalApplicationId())) {
                throw new BusinessException(ErrorMessageFactory.invalidParameterForAction("sourceInternalApplicationId"));
            }

            if (sourceApp.getEndpoints() != null) {
                sourceApp.getEndpoints().remove(endpoint);
            }
            applicationRepository.save(sourceApp);

            if (targetApp.getEndpoints() == null) {
                targetApp.setEndpoints(new HashSet<>());
            }
            targetApp.getEndpoints().add(endpoint);
            applicationRepository.save(targetApp);

            var originalOnboardResponse = endpoint.asOnboardingResponse(true);
            var routerOnboardJson = targetApp.createOnboardResponseForRouterDevice(originalOnboardResponse);
            endpoint.setOnboardResponseForRouterDevice(routerOnboardJson);
            endpointRepository.save(endpoint);

            endpointService.sendCapabilities(targetApp, endpoint);

            businessOperationLogService.log(new EndpointLogInformation(endpoint.getExternalEndpointId(), endpoint.getAgrirouterEndpointId()),
                    "Endpoint migrated from application '%s' to '%s'.", sourceApp.getInternalApplicationId(), targetApp.getInternalApplicationId());
        }
    }

}
