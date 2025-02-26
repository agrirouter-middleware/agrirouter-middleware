package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.impl.common.signing.SecurityKeyCreationService;
import de.agrirouter.middleware.api.IdFactory;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.api.events.ResendCapabilitiesForApplicationEvent;
import de.agrirouter.middleware.api.events.RouterDeviceAddedEvent;
import de.agrirouter.middleware.api.logging.ApplicationLogInformation;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.business.parameters.AddRouterDeviceParameters;
import de.agrirouter.middleware.domain.*;
import de.agrirouter.middleware.persistence.jpa.ApplicationRepository;
import de.agrirouter.middleware.persistence.jpa.RouterDeviceRepository;
import de.agrirouter.middleware.persistence.jpa.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EndpointService endpointService;
    private final RouterDeviceRepository routerDeviceRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              TenantRepository tenantRepository,
                              ApplicationEventPublisher applicationEventPublisher,
                              BusinessOperationLogService businessOperationLogService,
                              EndpointService endpointService,
                              RouterDeviceRepository routerDeviceRepository) {
        this.applicationRepository = applicationRepository;
        this.tenantRepository = tenantRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.businessOperationLogService = businessOperationLogService;
        this.endpointService = endpointService;
        this.routerDeviceRepository = routerDeviceRepository;
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
            checkCertificatesForApplication(application);
            applicationRepository.save(application);
            businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "Application created.");
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindTenant(principal.getName()));
        }
    }

    /**
     * Check if it is possible to create a private and a public key for the application.
     *
     * @param application The application to check.
     */
    private void checkCertificatesForApplication(Application application) {
        var securityKeyCreationService = new SecurityKeyCreationService();
        try {
            log.info("Creating private key for application {}.", application.getApplicationId());
            securityKeyCreationService.createPrivateKey(application.getPrivateKey());
        } catch (Exception e) {
            throw new BusinessException(ErrorMessageFactory.couldNotCreatePrivateKeyForApplication(application.getApplicationId(), application.getVersionId()));
        }
        try {
            log.info("Creating public key for application {}.", application.getApplicationId());
            securityKeyCreationService.createPublicKey(application.getPublicKey());
        } catch (Exception e) {
            throw new BusinessException(ErrorMessageFactory.couldNotCreatePublicKeyForApplication(application.getApplicationId(), application.getVersionId()));
        }
    }

    /**
     * Update the existing application.
     *
     * @param application The application to update.
     */
    public void update(Application application) {
        checkCertificatesForApplication(application);
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
     * Delegate.
     *
     * @return The list of applications.
     */
    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    /**
     * Find an application for the given endpoint.
     *
     * @param endpoint -
     * @return -
     */
    public Application findByEndpoint(Endpoint endpoint) {
        var application = applicationRepository.findByEndpointsContains(endpoint);
        if (application.isPresent()) {
            return application.get();
        } else {
            throw new BusinessException(ErrorMessageFactory.couldNotFindApplication());
        }
    }

    /**
     * Add a new router device to the application.
     *
     * @param addRouterDeviceParameters -
     */
    @Transactional
    public void addRouterDevice(AddRouterDeviceParameters addRouterDeviceParameters) {
        Optional<Application> optionalApplication = applicationRepository.findByInternalApplicationIdAndTenantTenantId(addRouterDeviceParameters.getInternalApplicationId(), addRouterDeviceParameters.getTenantId());
        if (optionalApplication.isPresent()) {
            final var application = optionalApplication.get();
            if (application.usesRouterDevice()) {
                if (routerDeviceRepository.existsByIdNotAndConnectionCriteria_ClientId(application.getApplicationSettings().getRouterDevice().getId(), addRouterDeviceParameters.getClientId())) {
                    throw new BusinessException(ErrorMessageFactory.routerDeviceAlreadyExists(addRouterDeviceParameters.getClientId()));
                } else {
                    log.debug("The current application already has a router device, therefore updating it.");
                    var formerRouterDevice = application.getApplicationSettings().getRouterDevice();
                    routerDeviceRepository.delete(formerRouterDevice);
                }
            }
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

    /**
     * Delete the application incl. all the endpoints and other data.
     *
     * @param internalApplicationId The ID of the application.
     */
    @Async
    @Transactional
    public void delete(String internalApplicationId) {
        Application application = find(internalApplicationId);
        application.getEndpoints().forEach(endpoint -> endpointService.delete(endpoint.getExternalEndpointId()));
        businessOperationLogService.log(new ApplicationLogInformation(application.getInternalApplicationId(), application.getApplicationId()), "Application deleted.");
        applicationRepository.delete(application);
    }

    /**
     * Count the number of applications.
     *
     * @return Number of applications.
     */
    public long getNrOfApplications() {
        return applicationRepository.count();
    }
}
