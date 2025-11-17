package de.agrirouter.middleware.business;

import com.dke.data.agrirouter.api.enums.ApplicationType;
import com.dke.data.agrirouter.api.enums.SecuredOnboardingResponseType;
import com.dke.data.agrirouter.api.service.onboard.secured.AuthorizationRequestService;
import com.dke.data.agrirouter.api.service.parameters.AuthorizationRequestParameters;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.business.global.OnboardStateContainer;
import de.agrirouter.middleware.domain.Application;
import de.agrirouter.middleware.domain.ApplicationSettings;
import de.agrirouter.middleware.domain.Tenant;
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
        String customRedirectUrl = "http://localhost:5117/unsecured/api/callback";
        String applicationId = "test-app-id";
        String externalEndpointId = "test-endpoint-id";
        String tenantId = "test-tenant-id";
        String state = "random-state";

        Application application = createApplication(applicationId, tenantId);
        
        when(onboardStateContainer.push(any(), any(), any(), eq(customRedirectUrl)))
                .thenReturn(state);
        when(authorizationRequestService.getAuthorizationRequestURL(any()))
                .thenReturn("http://agrirouter.example/authorize?state=" + state);

        // Act
        securedOnboardProcessService.generateAuthorizationUrl(application, externalEndpointId, customRedirectUrl);

        // Assert
        ArgumentCaptor<AuthorizationRequestParameters> captor = ArgumentCaptor.forClass(AuthorizationRequestParameters.class);
        verify(authorizationRequestService).getAuthorizationRequestURL(captor.capture());
        
        AuthorizationRequestParameters capturedParams = captor.getValue();
        assertThat(capturedParams.getRedirectUri()).isEqualTo(customRedirectUrl);
        assertThat(capturedParams.getApplicationId()).isEqualTo(applicationId);
        assertThat(capturedParams.getResponseType()).isEqualTo(SecuredOnboardingResponseType.ONBOARD);
        assertThat(capturedParams.getState()).isEqualTo(state);
    }

    @Test
    void generateAuthorizationUrl_withoutRedirectUrlParameter_shouldUseApplicationSettingsRedirectUrl() {
        // Arrange
        String applicationSettingsRedirectUrl = "https://production.example.com/callback";
        String applicationId = "test-app-id";
        String externalEndpointId = "test-endpoint-id";
        String tenantId = "test-tenant-id";
        String state = "random-state";

        Application application = createApplicationWithRedirectUrl(applicationId, tenantId, applicationSettingsRedirectUrl);
        
        when(onboardStateContainer.push(any(), any(), any(), eq(applicationSettingsRedirectUrl)))
                .thenReturn(state);
        when(authorizationRequestService.getAuthorizationRequestURL(any()))
                .thenReturn("http://agrirouter.example/authorize?state=" + state);

        // Act
        securedOnboardProcessService.generateAuthorizationUrl(application, externalEndpointId, null);

        // Assert
        ArgumentCaptor<AuthorizationRequestParameters> captor = ArgumentCaptor.forClass(AuthorizationRequestParameters.class);
        verify(authorizationRequestService).getAuthorizationRequestURL(captor.capture());
        
        AuthorizationRequestParameters capturedParams = captor.getValue();
        assertThat(capturedParams.getRedirectUri()).isEqualTo(applicationSettingsRedirectUrl);
        assertThat(capturedParams.getApplicationId()).isEqualTo(applicationId);
        assertThat(capturedParams.getResponseType()).isEqualTo(SecuredOnboardingResponseType.ONBOARD);
        assertThat(capturedParams.getState()).isEqualTo(state);
    }

    @Test
    void generateAuthorizationUrl_withEmptyRedirectUrlParameter_shouldUseApplicationSettingsRedirectUrl() {
        // Arrange
        String applicationSettingsRedirectUrl = "https://production.example.com/callback";
        String applicationId = "test-app-id";
        String externalEndpointId = "test-endpoint-id";
        String tenantId = "test-tenant-id";
        String state = "random-state";

        Application application = createApplicationWithRedirectUrl(applicationId, tenantId, applicationSettingsRedirectUrl);
        
        when(onboardStateContainer.push(any(), any(), any(), eq(applicationSettingsRedirectUrl)))
                .thenReturn(state);
        when(authorizationRequestService.getAuthorizationRequestURL(any()))
                .thenReturn("http://agrirouter.example/authorize?state=" + state);

        // Act
        securedOnboardProcessService.generateAuthorizationUrl(application, externalEndpointId, "");

        // Assert
        ArgumentCaptor<AuthorizationRequestParameters> captor = ArgumentCaptor.forClass(AuthorizationRequestParameters.class);
        verify(authorizationRequestService).getAuthorizationRequestURL(captor.capture());
        
        AuthorizationRequestParameters capturedParams = captor.getValue();
        assertThat(capturedParams.getRedirectUri()).isEqualTo(applicationSettingsRedirectUrl);
    }

    @Test
    void generateAuthorizationUrl_withNoApplicationType_shouldThrowBusinessException() {
        // Arrange
        Application application = new Application();
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
        Application application = new Application();
        application.setApplicationId(applicationId);
        application.setApplicationType(ApplicationType.FARMING_SOFTWARE);
        
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        application.setTenant(tenant);
        
        ApplicationSettings settings = new ApplicationSettings();
        settings.setRedirectUrl(redirectUrl);
        application.setApplicationSettings(settings);
        
        return application;
    }
}
