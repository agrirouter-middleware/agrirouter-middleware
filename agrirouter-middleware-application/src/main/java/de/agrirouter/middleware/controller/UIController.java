package de.agrirouter.middleware.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * Abstract UI controller.
 */
public abstract class UIController {

    @Autowired
    private Environment environment;

    protected String getActiveProfiles() {
        return "Active profiles = " + String.join(", ", environment.getActiveProfiles());
    }

}
