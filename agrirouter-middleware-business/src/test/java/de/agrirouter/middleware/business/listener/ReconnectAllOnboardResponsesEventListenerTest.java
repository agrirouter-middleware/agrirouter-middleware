package de.agrirouter.middleware.business.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.business.ApplicationService;
import de.agrirouter.middleware.business.EndpointService;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconnectAllOnboardResponsesEventListenerTest {

    @Mock
    private ApplicationService applicationService;
    @Mock
    private EndpointService endpointService;
    @Mock
    private MqttClientManagementService mqttClientManagementService;
    @Mock
    private Endpoint endpoint;
    @Mock
    private Mqtt3AsyncClient mqttClient;

    private ReconnectAllOnboardResponsesEventListener eventListener;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        eventListener = new ReconnectAllOnboardResponsesEventListener(applicationService, endpointService, mqttClientManagementService);
        logger = (Logger) LoggerFactory.getLogger(ReconnectAllOnboardResponsesEventListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void givenMissingRouterDevice_whenReconnectingAllOnboardResponses_thenLogsErrorWithoutStacktrace() {
        when(applicationService.findAll()).thenReturn(List.of());
        when(endpointService.findAll()).thenReturn(List.of(endpoint));
        when(endpoint.isDeactivated()).thenReturn(false);
        when(mqttClientManagementService.get(endpoint)).thenReturn(Optional.of(mqttClient));
        when(endpoint.asOnboardingResponse()).thenThrow(new BusinessException(ErrorMessageFactory.missingRouterDevice("datalogisk:DK:5020")));

        eventListener.reconnectAllOnboardResponses();

        var errorLogs = listAppender.list.stream()
                .filter(logEvent -> Level.ERROR.equals(logEvent.getLevel()))
                .toList();
        assertEquals(1, errorLogs.size());
        assertTrue(errorLogs.get(0).getFormattedMessage().contains("Could not reconnect a client, please check the client to avoid data loss."));
        assertTrue(errorLogs.get(0).getFormattedMessage().contains("[ERR_00038] Could not find the router device for the endpoint with the external endpoint ID 'datalogisk:DK:5020'."));
        assertNull(errorLogs.get(0).getThrowableProxy());
    }
}
