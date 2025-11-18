package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.enums.SecuredOnboardingResponseType;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.api.service.parameters.AuthorizationRequestParameters;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.business.global.OnboardStateContainer;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.ApplicationSettings;
import de.agrirouter.middleware.domain.Tenant;
import de.agrirouter.middleware.domain.enums.ApplicationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecuredOnboardProcessServiceTest {

    private AuthorizationRequestService authorizationRequestService;
    private OnboardStateContainer onboardStateContainer;
    private SecuredOnboardProcessService securedOnboardProcessService;

    @BeforeEach
    void setUp() {
        authorizationRequestService = mock(AuthorizationRequestService.class);
        onboardStateContainer = mock(OnboardStateContainer.class);
        securedOnboardProcessService = new SecuredOnboardProcessService(
                authorizationRequestService,
                onboardStateContainer,
                null, // securedOnboardProcessIntegrationService
                null, // applicationRepository
                null, // endpointService
                null, // businessOperationLogService
                null  // gson
        );
    }

    @Test
    void generateAuthorizationUrl_withRedirectUrlParameter_shouldSetRedirectUri() {
        // Arrange
        var customRedirectUrl = "http://localhost:5117/unsecured/api/callback";
        var applicationId = "test-app-id";
        var externalEndpointId = "test-endpoint-id";
        var tenantId = "test-tenant-id";
        var state = "random-state";

        var application = createApplication(applicationId, tenantId);
        
        when(onboardStateContainer.push(any(), any(), any(), eq(customRedirectUrl)))
                .thenReturn(state);
        when(authorizationRequestService.getAuthorizationRequestURL(any()))
                .thenReturn("http://agrirouter.example/authorize?state=" + state);

        // Act
        securedOnboardProcessService.generateAuthorizationUrl(application, externalEndpointId, customRedirectUrl);

        // Assert
        var captor = ArgumentCaptor.forClass(AuthorizationRequestParameters.class);
        verify(authorizationRequestService).getAuthorizationRequestURL(captor.capture());
        
        var capturedParams = captor.getValue();
        assertThat(capturedParams.getRedirectUri()).isEqualTo(customRedirectUrl);
        assertThat(capturedParams.getApplicationId()).isEqualTo(applicationId);
        assertThat(capturedParams.getResponseType()).isEqualTo(SecuredOnboardingResponseType.ONBOARD);
        assertThat(capturedParams.getState()).isEqualTo(state);
    }

    @Test
    void generateAuthorizationUrl_withoutRedirectUrlParameter_shouldUseApplicationSettingsRedirectUrl() {
        // Arrange
        var applicationSettingsRedirectUrl = "https://production.example.com/callback";
        var applicationId = "test-app-id";
        var externalEndpointId = "test-endpoint-id";
        var tenantId = "test-tenant-id";
        var state = "random-state";

        var application = createApplicationWithRedirectUrl(applicationId, tenantId, applicationSettingsRedirectUrl);
        
        when(onboardStateContainer.push(any(), any(), any(), eq(applicationSettingsRedirectUrl)))
                .thenReturn(state);
        when(authorizationRequestService.getAuthorizationRequestURL(any()))
                .thenReturn("http://agrirouter.example/authorize?state=" + state);

        // Act
        securedOnboardProcessService.generateAuthorizationUrl(application, externalEndpointId, null);

        // Assert
        var captor = ArgumentCaptor.forClass(AuthorizationRequestParameters.class);
        verify(authorizationRequestService).getAuthorizationRequestURL(captor.capture());
        
        var capturedParams = captor.getValue();
        assertThat(capturedParams.getRedirectUri()).isEqualTo(applicationSettingsRedirectUrl);
        assertThat(capturedParams.getApplicationId()).isEqualTo(applicationId);
        assertThat(capturedParams.getResponseType()).isEqualTo(SecuredOnboardingResponseType.ONBOARD);
        assertThat(capturedParams.getState()).isEqualTo(state);
    }

    @Test
    void generateAuthorizationUrl_withEmptyRedirectUrlParameter_shouldUseApplicationSettingsRedirectUrl() {
        // Arrange
        var applicationSettingsRedirectUrl = "https://production.example.com/callback";
        var applicationId = "test-app-id";
        var externalEndpointId = "test-endpoint-id";
        var tenantId = "test-tenant-id";
        var state = "random-state";

        var application = createApplicationWithRedirectUrl(applicationId, tenantId, applicationSettingsRedirectUrl);
        
        when(onboardStateContainer.push(any(), any(), any(), eq(applicationSettingsRedirectUrl)))
                .thenReturn(state);
        when(authorizationRequestService.getAuthorizationRequestURL(any()))
                .thenReturn("http://agrirouter.example/authorize?state=" + state);

        // Act
        securedOnboardProcessService.generateAuthorizationUrl(application, externalEndpointId, "");

        // Assert
        var captor = ArgumentCaptor.forClass(AuthorizationRequestParameters.class);
        verify(authorizationRequestService).getAuthorizationRequestURL(captor.capture());
        
        var capturedParams = captor.getValue();
        assertThat(capturedParams.getRedirectUri()).isEqualTo(applicationSettingsRedirectUrl);
    }

    @Test
    void generateAuthorizationUrl_withNoApplicationType_shouldThrowBusinessException() {
        // Arrange
        var application = new Application();
        application.setApplicationType(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                securedOnboardProcessService.generateAuthorizationUrl(application, "endpoint-id", "http://localhost/callback")
        );
    }

    private Application createApplication(String applicationId, String tenantId) {
        return createApplicationWithRedirectUrl(applicationId, tenantId, "http://default.example.com/callback");
    }

    private Application createApplicationWithRedirectUrl(String applicationId, String tenantId, String redirectUrl) {
        var application = new Application();
        application.setApplicationId(applicationId);
        application.setApplicationType(ApplicationType.FARMING_SOFTWARE);
        
        var tenant = new Tenant();
        tenant.setTenantId(tenantId);
        application.setTenant(tenant);
        
        var settings = new ApplicationSettings();
        settings.setRedirectUrl(redirectUrl);
        application.setApplicationSettings(settings);
        
        return application;
    }
}
