package de.agrirouter.middleware.business.security;

import lombok.Getter;

/**
 * Roles for the application.
 */
public enum Roles {
    MONITORING("monitoring"), USER("user"), DEFAULT("default");

    @Getter
    private final String key;

    Roles(String key) {
        this.key = key;
    }
}
