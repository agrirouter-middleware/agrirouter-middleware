package de.agrirouter.middleware.api;

/**
 * Central routing definitions.
 */
public class Routes {

    public static final class UnsecuredEndpoints {
        public static final String ALL_REQUESTS = "/unsecured/api";
        public static final String ERROR = "error";
        public static final String HOME = "home";
        public static final String ONBOARD_PROCESS_RESULT = "onboard-process-result";

    }

    public static final class SecuredRestEndpoints {
        public static final String ALL_REQUESTS = "/secured/api";
    }

    public static class UserInterface {
        public static final String APPLICATIONS = "/applications";
        public static final String ENDPOINTS = "/endpoints";
        public static final String ENDPOINT_DASHBOARD = "/endpoint-dashboard";
        public static class ThymeleafRouting {
            public static final String APPLICATIONS = "applications";
            public static final String ENDPOINTS = "endpoints";
            public static final String ENDPOINT_DASHBOARD = "endpoint-dashboard";
            public static final String APPLICATION_STATISTICS = "application-statistics";
        }
    }


    public static final class MonitoringEndpoints {
        public static final String ACTUATOR = "/actuator";
        public static final String ALL_REQUESTS = "/monitoring/api";
    }

}
