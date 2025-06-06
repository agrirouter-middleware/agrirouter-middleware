package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.IdFactory;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.dto.TenantRegistrationResult;
import de.agrirouter.middleware.business.security.TenantPrincipal;
import de.agrirouter.middleware.domain.Tenant;
import de.agrirouter.middleware.persistence.jpa.TenantRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handle all business requests regarding the tenant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService implements UserDetailsService {

    public static final int DEFAULT_ACCESS_TOKEN_LENGTH = 32;

    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.actuator.tenant-id:NONE}")
    private String externalActuatorTenantId;

    @Value("${app.actuator.access-token:NONE}")
    private String externalActuatorAccessToken;

    /**
     * Initialize the default tenant.
     */
    @PostConstruct
    protected void generateDefaultTenants() {
        generateDefaultTenant();
        generateDefaultMonitoringTenant();
    }

    private void generateDefaultMonitoringTenant() {
        if (tenantRepository.findByMonitoringAccessIsTrue().isEmpty()) {
            Tenant tenant = new Tenant();
            tenant.setTenantId(IdFactory.tenantId());
            tenant.setName(generateAccessToken(24));
            tenant.setGeneratedTenant(true);
            tenant.setMonitoringAccess(true);
            final var accessToken = generateAccessToken(DEFAULT_ACCESS_TOKEN_LENGTH);
            tenant.setAccessToken(passwordEncoder.encode(accessToken));
            tenantRepository.save(tenant);
            log.info("#######################################################################################################################################################");
            log.info("#");
            log.info("# Generated default tenant for monitoring!");
            log.info("# Name: {}", tenant.getName());
            log.info("# Tenant ID: {}", tenant.getTenantId());
            log.info("# Access token: {}", accessToken);
            log.info("#");
            log.info("# NOTE");
            log.info("# The password for the default monitoring tenant is stored as hash, therefore it will be printed only (!) THIS (!) time and never again.");
            log.info("# If you need to reset the password and generate a new one, remove the default tenant from the database so it will be generated and printed again.");
            log.info("#######################################################################################################################################################");
        }
    }

    private void generateDefaultTenant() {
        if (tenantRepository.findByDefaultTenantIsTrue().isEmpty()) {
            Tenant tenant = new Tenant();
            tenant.setTenantId(IdFactory.tenantId());
            tenant.setName(generateAccessToken(24));
            tenant.setGeneratedTenant(true);
            tenant.setDefaultTenant(true);
            final var accessToken = generateAccessToken(DEFAULT_ACCESS_TOKEN_LENGTH);
            tenant.setAccessToken(passwordEncoder.encode(accessToken));
            tenantRepository.save(tenant);
            log.info("#######################################################################################################################################################");
            log.info("#");
            log.info("# Generated default tenant!");
            log.info("# Name: {}", tenant.getName());
            log.info("# Tenant ID: {}", tenant.getTenantId());
            log.info("# Access token: {}", accessToken);
            log.info("#");
            log.info("# NOTE");
            log.info("# The password for the default tenant is stored as hash, therefore it will be printed only (!) THIS (!) time and never again.");
            log.info("# If you need to reset the password and generate a new one, remove the default tenant from the database so it will be generated and printed again.");
            log.info("#######################################################################################################################################################");
        }
    }

    /**
     * Register a tenant.
     *
     * @param name Name of the tenant, has to be unique.
     * @return The access token for the tenant.
     */

    public TenantRegistrationResult register(String name) {
        name = name.trim();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ErrorMessageFactory.invalidParameterForAction("name"));
        } else {
            if (tenantRepository.findTenantByNameIgnoreCase(name).isPresent()) {
                throw new BusinessException(ErrorMessageFactory.tenantAlreadyExists(name));
            } else {
                String accessToken = generateAccessToken(DEFAULT_ACCESS_TOKEN_LENGTH);
                String tenantId = IdFactory.tenantId();
                final var tenant = new Tenant();
                tenant.setName(name);
                tenant.setAccessToken(passwordEncoder.encode(accessToken));
                tenant.setTenantId(tenantId);
                final var t = tenantRepository.save(tenant);
                TenantRegistrationResult tenantRegistrationResult = new TenantRegistrationResult();
                tenantRegistrationResult.setTenantId(t.getTenantId());
                tenantRegistrationResult.setAccessToken(accessToken);
                log.info("#######################################################################################################################################################");
                log.info("#");
                log.info("# A new tenant was registered!");
                log.info("# Name: {}", tenant.getName());
                log.info("# Tenant ID: {}", tenant.getTenantId());
                log.info("#");
                log.info("# NOTE");
                log.info("# The password for the tenant is stored as hash, therefore it will be printed only during setup.");
                log.info("# If you need to reset the password and generate a new one, remove the default tenant from the database so it will be generated and printed again.");
                log.info("#######################################################################################################################################################");
                return tenantRegistrationResult;
            }
        }
    }


    private String generateAccessToken(int defaultAccessTokenLength) {
        return RandomStringUtils.secureStrong().nextAlphanumeric(defaultAccessTokenLength);
    }

    @Override
    public UserDetails loadUserByUsername(String tenantId) throws UsernameNotFoundException {
        if (StringUtils.isNotBlank(externalActuatorTenantId) && StringUtils.isNotBlank(externalActuatorAccessToken) &&
                !externalActuatorTenantId.equals("NONE") && !externalActuatorAccessToken.equals("NONE")) {
            if (externalActuatorTenantId.equals(tenantId)) {
                var externalActuatorTenant = new Tenant();
                externalActuatorTenant.setTenantId(externalActuatorTenantId);
                externalActuatorTenant.setAccessToken(passwordEncoder.encode(externalActuatorAccessToken));
                externalActuatorTenant.setMonitoringAccess(true);
                return new TenantPrincipal(externalActuatorTenant);
            } else {
                log.trace("This was not the external actuator tenant ID. Using the generated tenant information instead.");
            }
        } else {
            log.trace("External actuator tenant is not configured. If you want to use the actuator, please configure the tenant ID and access token and use the defined profile.");
        }
        final var optionalTenant = tenantRepository.findTenantByTenantId(tenantId);
        if (optionalTenant.isEmpty()) {
            throw new UsernameNotFoundException(String.format("Tenant with name '%s' has not been found.", tenantId));
        } else {
            return new TenantPrincipal(optionalTenant.get());
        }
    }

    /**
     * List all existing tenants.
     *
     * @return -
     */
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    /**
     * Reset the Password for a tenant.
     *
     * @param tenantId Tenant ID
     * @return The new access token.
     */
    public String resetPassword(String tenantId) {
        final var optionalTenant = tenantRepository.findTenantByTenantId(tenantId);
        if (optionalTenant.isEmpty()) {
            throw new BusinessException(ErrorMessageFactory.couldNotFindTenant(tenantId));
        } else {
            final var tenant = optionalTenant.get();
            final var accessToken = generateAccessToken(DEFAULT_ACCESS_TOKEN_LENGTH);
            tenant.setAccessToken(passwordEncoder.encode(accessToken));
            tenantRepository.save(tenant);
            log.info("#######################################################################################################################################################");
            log.info("#");
            log.info("# The password for the tenant '{}' was reset!", tenant.getName());
            log.info("# Tenant ID: {}", tenant.getTenantId());
            log.info("#");
            log.info("# NOTE");
            log.info("# The password for the tenant is stored as hash, therefore it will be printed only during setup.");
            log.info("# If you need to reset the password and generate a new one, remove the default tenant from the database so it will be generated and printed again.");
            log.info("#######################################################################################################################################################");
            return accessToken;
        }
    }

    /**
     * Count the number of tenants.
     *
     * @return The number of tenants.
     */
    public long getNrOfTenants() {
        return tenantRepository.count();
    }
}
