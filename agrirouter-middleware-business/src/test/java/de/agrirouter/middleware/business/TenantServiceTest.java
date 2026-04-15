package de.agrirouter.middleware.business;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.domain.Tenant;
import de.agrirouter.middleware.persistence.jpa.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void register_withValidName_createsTenantAndReturnsResult() {
        var name = "MyTenant";
        when(tenantRepository.findTenantByNameIgnoreCase(name)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-token");
        var savedTenant = new Tenant();
        savedTenant.setTenantId("generated-tenant-id");
        savedTenant.setName(name);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

        var result = tenantService.register(name);

        assertThat(result).isNotNull();
        assertThat(result.getTenantId()).isEqualTo("generated-tenant-id");
        assertThat(result.getAccessToken()).isNotBlank();
        var captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(name);
    }

    @Test
    void register_withNameRequiringTrim_trimsTenantName() {
        var paddedName = "  MyTenant  ";
        var trimmedName = "MyTenant";
        when(tenantRepository.findTenantByNameIgnoreCase(trimmedName)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-token");
        var savedTenant = new Tenant();
        savedTenant.setTenantId("generated-id");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

        tenantService.register(paddedName);

        var captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(trimmedName);
    }

    @Test
    void register_withBlankName_throwsBusinessException() {
        assertThrows(BusinessException.class, () -> tenantService.register("   "));
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void register_withEmptyName_throwsBusinessException() {
        assertThrows(BusinessException.class, () -> tenantService.register(""));
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void register_withAlreadyExistingName_throwsBusinessException() {
        var name = "ExistingTenant";
        var existingTenant = new Tenant();
        existingTenant.setName(name);
        when(tenantRepository.findTenantByNameIgnoreCase(name)).thenReturn(Optional.of(existingTenant));

        assertThrows(BusinessException.class, () -> tenantService.register(name));
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void loadUserByUsername_withExistingTenant_returnsTenantPrincipal() {
        var tenantId = "tenant-123";
        var tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setAccessToken("hashed-token");
        when(tenantRepository.findTenantByTenantId(tenantId)).thenReturn(Optional.of(tenant));

        var result = tenantService.loadUserByUsername(tenantId);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(tenantId);
    }

    @Test
    void loadUserByUsername_withNonExistingTenant_throwsUsernameNotFoundException() {
        var tenantId = "unknown-tenant";
        when(tenantRepository.findTenantByTenantId(tenantId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> tenantService.loadUserByUsername(tenantId));
    }

    @Test
    void loadUserByUsername_withExternalActuatorTenantId_returnsExternalActuatorPrincipal() {
        var actuatorTenantId = "actuator-tenant";
        var actuatorToken = "actuator-token";
        ReflectionTestUtils.setField(tenantService, "externalActuatorTenantId", actuatorTenantId);
        ReflectionTestUtils.setField(tenantService, "externalActuatorAccessToken", actuatorToken);
        when(passwordEncoder.encode(actuatorToken)).thenReturn("encoded-actuator-token");

        var result = tenantService.loadUserByUsername(actuatorTenantId);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(actuatorTenantId);
        verify(tenantRepository, never()).findTenantByTenantId(any());
    }

    @Test
    void loadUserByUsername_withExternalActuatorTenantId_butDifferentTenantId_fallsBackToRepository() {
        var actuatorTenantId = "actuator-tenant";
        var actuatorToken = "actuator-token";
        var requestedTenantId = "other-tenant";
        ReflectionTestUtils.setField(tenantService, "externalActuatorTenantId", actuatorTenantId);
        ReflectionTestUtils.setField(tenantService, "externalActuatorAccessToken", actuatorToken);
        var tenant = new Tenant();
        tenant.setTenantId(requestedTenantId);
        when(tenantRepository.findTenantByTenantId(requestedTenantId)).thenReturn(Optional.of(tenant));

        var result = tenantService.loadUserByUsername(requestedTenantId);

        assertThat(result.getUsername()).isEqualTo(requestedTenantId);
        verify(tenantRepository).findTenantByTenantId(requestedTenantId);
    }

    @Test
    void findAll_delegatesToRepository() {
        var tenants = List.of(new Tenant(), new Tenant());
        when(tenantRepository.findAll()).thenReturn(tenants);

        var result = tenantService.findAll();

        assertThat(result).hasSize(2);
        verify(tenantRepository).findAll();
    }

    @Test
    void resetPassword_withExistingTenant_returnsNewAccessToken() {
        var tenantId = "tenant-456";
        var tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setName("SomeTenant");
        when(tenantRepository.findTenantByTenantId(tenantId)).thenReturn(Optional.of(tenant));
        when(passwordEncoder.encode(anyString())).thenReturn("new-encoded-token");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        var result = tenantService.resetPassword(tenantId);

        assertThat(result).isNotBlank();
        assertThat(result).hasSize(TenantService.DEFAULT_ACCESS_TOKEN_LENGTH);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void resetPassword_withNonExistingTenant_throwsBusinessException() {
        var tenantId = "ghost-tenant";
        when(tenantRepository.findTenantByTenantId(tenantId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> tenantService.resetPassword(tenantId));
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void getNrOfTenants_delegatesToRepository() {
        when(tenantRepository.count()).thenReturn(5L);

        var result = tenantService.getNrOfTenants();

        assertThat(result).isEqualTo(5L);
        verify(tenantRepository).count();
    }
}
