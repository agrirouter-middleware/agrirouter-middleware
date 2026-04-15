package de.agrirouter.middleware.business.security;

import de.agrirouter.middleware.domain.Tenant;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class TenantPrincipalTest {

    @Test
    void getAuthorities_forMonitoringTenant_returnsMonitoringRole() {
        var tenant = new Tenant();
        tenant.setTenantId("monitoring-tenant");
        tenant.setAccessToken("hashed-token");
        tenant.setMonitoringAccess(true);

        var principal = new TenantPrincipal(tenant);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(Roles.MONITORING.getKey());
    }

    @Test
    void getAuthorities_forDefaultTenant_returnsUserAndDefaultRoles() {
        var tenant = new Tenant();
        tenant.setTenantId("default-tenant");
        tenant.setAccessToken("hashed-token");
        tenant.setDefaultTenant(true);

        var principal = new TenantPrincipal(tenant);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .hasSize(2)
                .contains(Roles.USER.getKey())
                .contains(Roles.DEFAULT.getKey());
    }

    @Test
    void getAuthorities_forRegularTenant_returnsUserRoleOnly() {
        var tenant = new Tenant();
        tenant.setTenantId("regular-tenant");
        tenant.setAccessToken("hashed-token");

        var principal = new TenantPrincipal(tenant);

        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(Roles.USER.getKey());
    }

    @Test
    void getPassword_returnsAccessToken() {
        var tenant = new Tenant();
        tenant.setAccessToken("my-access-token");

        var principal = new TenantPrincipal(tenant);

        assertThat(principal.getPassword()).isEqualTo("my-access-token");
    }

    @Test
    void getUsername_returnsTenantId() {
        var tenant = new Tenant();
        tenant.setTenantId("tenant-id-123");

        var principal = new TenantPrincipal(tenant);

        assertThat(principal.getUsername()).isEqualTo("tenant-id-123");
    }

    @Test
    void isAccountNonExpired_alwaysReturnsTrue() {
        var principal = new TenantPrincipal(new Tenant());
        assertThat(principal.isAccountNonExpired()).isTrue();
    }

    @Test
    void isAccountNonLocked_alwaysReturnsTrue() {
        var principal = new TenantPrincipal(new Tenant());
        assertThat(principal.isAccountNonLocked()).isTrue();
    }

    @Test
    void isCredentialsNonExpired_alwaysReturnsTrue() {
        var principal = new TenantPrincipal(new Tenant());
        assertThat(principal.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void isEnabled_alwaysReturnsTrue() {
        var principal = new TenantPrincipal(new Tenant());
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void monitoringAccessTakesPrecedenceOverDefaultTenant() {
        var tenant = new Tenant();
        tenant.setMonitoringAccess(true);
        tenant.setDefaultTenant(true);

        var principal = new TenantPrincipal(tenant);

        // monitoringAccess check comes first in the implementation
        assertThat(principal.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(Roles.MONITORING.getKey());
    }
}
