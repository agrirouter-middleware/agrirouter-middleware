package de.agrirouter.middleware.api;

/**
 * Central routing definitions.
 */
public class Routes {

    public static final class Unsecured {
        public static final String API_PATH = "/unsecured/api";
    }

    public static final class Secured {
        public static final String APPLICATIONS = "/applications";
        public static final String ENDPOINTS = "/endpoints";
        public static final String ENDPOINT_DASHBOARD = "/endpoint-dashboard";
        public static final String API_PATH = "/secured/api";
    }

    public static class UI {

        public static final String HOME = "home";
        public static final String APPLICATIONS = "applications";
        public static final String ENDPOINTS = "endpoints";
        public static final String ONBOARD_PROCESS_RESULT = "onboard-process-result";
        public static final String ENDPOINT_DASHBOARD = "endpoint-dashboard";
        public static final String ERROR = "error";
    }


}
