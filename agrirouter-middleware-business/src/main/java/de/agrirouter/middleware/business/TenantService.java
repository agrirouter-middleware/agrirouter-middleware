package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.IdFactory;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.dto.TenantRegistrationResult;
import de.agrirouter.middleware.business.security.TenantPrincipal;
import de.agrirouter.middleware.domain.Tenant;
import de.agrirouter.middleware.persistence.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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
@Slf4j
@Service
public class TenantService implements UserDetailsService {

    public static final int DEFAULT_ACCESS_TOKEN_LENGTH = 32;

    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantService(TenantRepository tenantRepository,
                         PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Initialize the default tenant.
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
        } else {
            final var generatedTenant = tenantRepository.findTenantByGeneratedTenantIsTrue();
            log.info("#######################################################################################################################################################");
            log.info("#");
            log.info("# Generated default tenant!");
            //noinspection OptionalGetWithoutIsPresent
            log.info("# Name: {}", generatedTenant.get().getName());
            log.info("# Tenant ID: {}", generatedTenant.get().getTenantId());
            log.info("#");
            log.info("# NOTE");
            log.info("# The password for the default tenant is stored as hash, therefore it will be printed only during setup.");
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
}
