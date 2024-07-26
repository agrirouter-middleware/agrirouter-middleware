package de.agrirouter.middleware.config.env;

import com.dke.data.agrirouter.api.env.Environment;

/**
 * Abstraction of the QA environment, currently no overrides because the default is QA already.
 */
public abstract class Ar2QA implements Environment {

    private static final String ENV_BASE_URL = "https://app.qa.agrirouter.farm";
    private static final String API_PREFIX = "/api/v1.0";
    private static final String REGISTRATION_SERVICE_URL =
            "https://endpoint-service.qa.agrirouter.farm";

    @Override
    public String getEnvironmentBaseUrl() {
        return ENV_BASE_URL;
    }

    @Override
    public String getApiPrefix() {
        return API_PREFIX;
    }

    @Override
    public String getRegistrationServiceUrl() {
        return REGISTRATION_SERVICE_URL;
    }
}
