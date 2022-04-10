package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.IdFactory;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.ResendCapabilitiesForApplicationEvent;
import de.agrirouter.middleware.api.events.RouterDeviceAddedEvent;
import de.agrirouter.middleware.api.logging.ApplicationLogInformation;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.business.parameters.AddRouterDeviceParameters;
import de.agrirouter.middleware.domain.*;
import de.agrirouter.middleware.persistence.ApplicationRepository;
import de.agrirouter.middleware.persistence.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Encapsulate all asynchronous business actions for applications.
 */
@Slf4j
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TenantRepository tenantRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BusinessOperationLogService businessOperationLogService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              TenantRepository tenantRepository,
                              ApplicationEventPublisher applicationEventPublisher,
                              BusinessOperationLogService businessOperationLogService) {
        this.applicationRepository = applicationRepository;
        this.tenantRepository = tenantRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.businessOperationLogService = businessOperationLogService;
    }

    /**
     * Saving an application.
     *
     * @param principal   Authentication token.
     * @param application The application to save.
     */
    public void save(Principal principal, Application application) {
        final var alreadyExistingApplication = applicationRepository.findByApplicationIdAndVersionId(application.getApplicationId(), application.getVersionId());
        alreadyExistingApplication.ifPresent(a -> {
            throw new BusinessException(ErrorMessageFactory.applicationDoesAlreadyExist(a.getApplicationId(), a.getVersionId()));
        });
        final var optionalTenant = tenantRepository.findTenantByTenantId(principal.getName());
        if (optionalTenant.isPresent()) {
            application.setTenant(optionalTenant.get());
            application.setInternalApplicationId(IdFactory.applicationId());
            applicationRepository.save(application);
            businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "Application created.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindTenant(principal.getName()));
        }
    }

    /**
     * Update the existing application.
     *
     * @param application The application to update.
     */
    public void update(Application application) {
        applicationRepository.save(application);
        businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "Application updated.");
    }

    /**
     * Set the supported technical message types for the application. This will cause setting the capabilities for each and every endpoint if there are already some.
     *
     * @param principal                      Authentication token.
     * @param internalApplicationId          -
     * @param supportedTechnicalMessageTypes -
     */
    public void defineSupportedTechnicalMessageTypes(Principal principal, String internalApplicationId, Set<SupportedTechnicalMessageType> supportedTechnicalMessageTypes) {
        Optional<Application> optionalApplication = applicationRepository.findByInternalApplicationIdAndTenantTenantId(internalApplicationId, principal.getName());
        if (optionalApplication.isPresent()) {
            final var application = optionalApplication.get();
            application.setSupportedTechnicalMessageTypes(supportedTechnicalMessageTypes);
            final var savedApplication = applicationRepository.save(application);
            applicationEventPublisher.publishEvent(new ResendCapabilitiesForApplicationEvent(this, savedApplication.getInternalApplicationId()));
            businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "Supported technical message types defined.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
        }
    }

    /**
     * Find an application.
     *
     * @param internalApplicationId The ID of the application.
     * @param principal             The current tenant.
     * @return -
     */
    public Application find(String internalApplicationId, Principal principal) {
        final var optionalApplication = applicationRepository.findByInternalApplicationIdAndTenantTenantId(internalApplicationId, principal.getName());
        if (optionalApplication.isPresent()) {
            return optionalApplication.get();
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
        }
    }

    /**
     * Find an application.
     *
     * @param internalApplicationId The ID of the application.
     * @return -
     */
    public Application find(String internalApplicationId) {
        final var optionalApplication = applicationRepository.findByInternalApplicationId(internalApplicationId);
        if (optionalApplication.isPresent()) {
            return optionalApplication.get();
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
        }
    }

    /**
     * Delegate.
     *
     * @param principal Authentication token.
     * @return The list of applications.
     */
    public List<Application> findAll(Principal principal) {
        return applicationRepository.findAllByTenantTenantId(principal.getName());
    }

    /**
     * Find an application for the given endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public Optional<Application> findByEndpoint(Endpoint endpoint) {
        return applicationRepository.findByEndpointsContains(endpoint);
    }

    /**
     * Add a new router device to the application.
     *
     * @param addRouterDeviceParameters -
     */
    public void addRouterDevice(AddRouterDeviceParameters addRouterDeviceParameters) {
        Optional<Application> optionalApplication = applicationRepository.findByInternalApplicationIdAndTenantTenantId(addRouterDeviceParameters.getInternalApplicationId(), addRouterDeviceParameters.getTenantId());
        if (optionalApplication.isPresent()) {
            final var application = optionalApplication.get();
            final var routerDevice = new RouterDevice();
            routerDevice.setDeviceAlternateId(addRouterDeviceParameters.getDeviceAlternateId());
            final var authentication = new Authentication();
            authentication.setCertificate(addRouterDeviceParameters.getCertificate());
            authentication.setSecret(addRouterDeviceParameters.getSecret());
            authentication.setType(addRouterDeviceParameters.getType());
            routerDevice.setAuthentication(authentication);
            final var connectionCriteria = new ConnectionCriteria();
            connectionCriteria.setClientId(addRouterDeviceParameters.getClientId());
            connectionCriteria.setHost(addRouterDeviceParameters.getHost());
            connectionCriteria.setPort(addRouterDeviceParameters.getPort());
            routerDevice.setConnectionCriteria(connectionCriteria);
            if (null == application.getApplicationSettings()) {
                log.debug("The current application did not have application settings, therefore creating new ones.");
                application.setApplicationSettings(new ApplicationSettings());
            }
            application.getApplicationSettings().setRouterDevice(routerDevice);
            final var savedApplication = applicationRepository.save(application);
            businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "Added router device to the application.");
            applicationEventPublisher.publishEvent(new RouterDeviceAddedEvent(this, savedApplication.getInternalApplicationId()));
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
        }
    }
}
