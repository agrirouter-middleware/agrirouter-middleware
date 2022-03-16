package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.IdFactory;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.dto.TenantRegistrationResult;
import de.agrirouter.middleware.business.security.TenantPrincipal;
import de.agrirouter.middleware.businesslog.BusinessLogService;
import de.agrirouter.middleware.domain.Tenant;
import de.agrirouter.middleware.persistence.TenantRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Handle all business requests regarding the tenant.
 */
@Service
public class TenantService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenantService.class);
    public static final int DEFAULT_ACCESS_TOKEN_LENGTH = 32;

    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessLogService businessLogService;

    public TenantService(TenantRepository tenantRepository,
                         PasswordEncoder passwordEncoder,
                         BusinessLogService businessLogService) {
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessLogService = businessLogService;
    }

    /**
     *
     */
    @PostConstruct
    protected void generateDefaultTenant() {
        if (tenantRepository.findTenantByGeneratedTenantIsTrue().isEmpty()) {
            Tenant tenant = new Tenant();
            tenant.setTenantId(IdFactory.tenantId());
            tenant.setName(generateAccessToken(24));
            tenant.setGeneratedTenant(true);
            final var accessToken = generateAccessToken(DEFAULT_ACCESS_TOKEN_LENGTH);
            tenant.setAccessToken(passwordEncoder.encode(accessToken));
            tenantRepository.save(tenant);
            LOGGER.info("#######################################################################################################################################################");
            LOGGER.info("#");
            LOGGER.info("# Generated default tenant!");
            LOGGER.info("# Tenant ID: {}", tenant.getTenantId());
            LOGGER.info("# Name: {}", tenant.getName());
            LOGGER.info("# Access token: {}", accessToken);
            LOGGER.info("#");
            LOGGER.info("# NOTE");
            LOGGER.info("# The password for the default tenant is stored as hash, therefore it will be printed only (!) THIS (!) time and never again.");
            LOGGER.info("# If you need to reset the password and generate a new one, remove the default tenant from the database so it will be generated and printed again.");
            LOGGER.info("#######################################################################################################################################################");
        } else {
            final var generatedTenant = tenantRepository.findTenantByGeneratedTenantIsTrue();
            LOGGER.info("#######################################################################################################################################################");
            LOGGER.info("#");
            LOGGER.info("# Generated default tenant!");
            //noinspection OptionalGetWithoutIsPresent
            LOGGER.info("# Tenant ID: {}", generatedTenant.get().getTenantId());
            LOGGER.info("# Name: {}", generatedTenant.get().getName());
            LOGGER.info("#");
            LOGGER.info("# NOTE");
            LOGGER.info("# The password for the default tenant is stored as hash, therefore it will be printed only during setup.");
            LOGGER.info("# If you need to reset the password and generate a new one, remove the default tenant from the database so it will be generated and printed again.");
            LOGGER.info("#######################################################################################################################################################");
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
                businessLogService.tenantCreated(t);
                TenantRegistrationResult tenantRegistrationResult = new TenantRegistrationResult();
                tenantRegistrationResult.setTenantId(t.getTenantId());
                tenantRegistrationResult.setAccessToken(accessToken);
                return tenantRegistrationResult;
            }
        }
    }

    @NotNull
    private String generateAccessToken(int defaultAccessTokenLength) {
        return RandomStringUtils.random(defaultAccessTokenLength, true, true);
    }

    @Override
    public UserDetails loadUserByUsername(String tenantId) throws UsernameNotFoundException {
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
}
