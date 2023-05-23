package de.agrirouter.middleware.business.security;

import de.agrirouter.middleware.domain.Tenant;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * The tenant as principle for the application.
 */
public class TenantPrincipal implements UserDetails {

    private final Tenant tenant;

    public TenantPrincipal(Tenant tenant) {
        this.tenant = tenant;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (tenant.isMonitoringAccess()) {
            return Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority(Roles.MONITORING.getKey()));
        }
        if (tenant.isDefaultTenant()) {
            return Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority(Roles.DEFAULT.getKey()));
        }
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return tenant.getAccessToken();
    }

    @Override
    public String getUsername() {
        return tenant.getTenantId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
