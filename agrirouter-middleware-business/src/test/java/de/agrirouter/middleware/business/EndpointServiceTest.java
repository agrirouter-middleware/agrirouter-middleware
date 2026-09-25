package de.agrirouter.middleware.business;

import agrirouter.commons.MessageOuterClass;
import agrirouter.response.Response;
import com.dke.data.agrirouter.api.dto.encoding.DecodeMessageResponse;
import com.dke.data.agrirouter.api.service.messaging.encoding.DecodeMessageService;
import com.google.protobuf.Any;
import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.logging.BusinessOperationLogService;
import de.agrirouter.middleware.business.cache.endpoints.InternalEndpointCache;
import de.agrirouter.middleware.business.cache.events.BusinessEventsCache;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.integration.EndpointIntegrationService;
import de.agrirouter.middleware.integration.RevokeProcessIntegrationService;
import de.agrirouter.middleware.integration.ack.MessageWaitingForAcknowledgementService;
import de.agrirouter.middleware.integration.mqtt.MqttClientManagementService;
import de.agrirouter.middleware.integration.mqtt.health.HealthStatusIntegrationService;
import de.agrirouter.middleware.integration.mqtt.list_endpoints.ListEndpointsIntegrationService;
import de.agrirouter.middleware.persistence.jpa.ApplicationRepository;
import de.agrirouter.middleware.persistence.jpa.EndpointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EndpointServiceTest {

    private static final String AGRIROUTER_ENDPOINT_ID = "ar-endpoint-id";
    private static final String EXTERNAL_ENDPOINT_ID = "external-endpoint-id";

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private DecodeMessageService decodeMessageService;

    @Mock
    private EndpointIntegrationService endpointIntegrationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private MqttClientManagementService mqttClientManagementService;

    @Mock
    private HealthStatusIntegrationService healthStatusIntegrationService;

    @Mock
    private RevokeProcessIntegrationService revokeProcessIntegrationService;

    @Mock
    private BusinessOperationLogService businessOperationLogService;

    @Mock
    private MessageWaitingForAcknowledgementService messageWaitingForAcknowledgementService;

    @Mock
    private BusinessEventsCache businessEventsCache;

    @Mock
    private ListEndpointsIntegrationService listEndpointsIntegrationService;

    @Mock
    private RemoveEndpointDataService removeEndpointDataService;

    @Spy
    private InternalEndpointCache internalEndpointCache = new InternalEndpointCache();

    @InjectMocks
    private EndpointService endpointService;

    @Test
    void findByAgrirouterEndpointId_whenNotCachedYet_fetchesTheEndpointFromTheDatabase() {
        var endpoint = endpoint(EXTERNAL_ENDPOINT_ID, AGRIROUTER_ENDPOINT_ID);
        when(endpointRepository.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).thenReturn(Optional.of(endpoint));

        var result = endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);

        assertThat(result.getExternalEndpointId()).isEqualTo(EXTERNAL_ENDPOINT_ID);
        verify(endpointRepository).findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);
    }

    @Test
    void findByAgrirouterEndpointId_whenCalledTwice_hitsTheDatabaseOnlyOnce() {
        var endpoint = endpoint(EXTERNAL_ENDPOINT_ID, AGRIROUTER_ENDPOINT_ID);
        when(endpointRepository.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).thenReturn(Optional.of(endpoint));

        endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);
        var result = endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);

        assertThat(result.getExternalEndpointId()).isEqualTo(EXTERNAL_ENDPOINT_ID);
        verify(endpointRepository, times(1)).findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);
    }

    @Test
    void findByAgrirouterEndpointId_whenTheEndpointIsUnknown_throwsAndDoesNotCacheTheMiss() {
        when(endpointRepository.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID));

        assertThat(internalEndpointCache.getByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).isEmpty();
    }

    @Test
    void save_afterTheEndpointWasCached_servesTheUpdatedEndpoint() {
        var endpoint = endpoint(EXTERNAL_ENDPOINT_ID, AGRIROUTER_ENDPOINT_ID);
        when(endpointRepository.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).thenReturn(Optional.of(endpoint));
        endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);

        var updatedEndpoint = endpoint(EXTERNAL_ENDPOINT_ID, AGRIROUTER_ENDPOINT_ID);
        updatedEndpoint.setDeactivated(true);
        when(endpointRepository.save(updatedEndpoint)).thenReturn(updatedEndpoint);
        endpointService.save(updatedEndpoint);

        assertThat(endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID).isDeactivated()).isTrue();
    }

    @Test
    void deleteEndpointDataFromTheMiddlewareByAgrirouterId_removesTheEndpointFromTheCache() {
        var endpoint = endpoint(EXTERNAL_ENDPOINT_ID, AGRIROUTER_ENDPOINT_ID);
        when(endpointRepository.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).thenReturn(Optional.of(endpoint));
        when(endpointRepository.findAllByExternalEndpointId(EXTERNAL_ENDPOINT_ID)).thenReturn(List.of(endpoint));
        endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);

        endpointService.deleteEndpointDataFromTheMiddlewareByAgrirouterId(AGRIROUTER_ENDPOINT_ID);

        assertThat(internalEndpointCache.getByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).isEmpty();
        assertThat(internalEndpointCache.get(EXTERNAL_ENDPOINT_ID)).isEmpty();
    }

    @Test
    void updateErrors_removesTheEndpointFromTheCache() {
        var endpoint = endpoint(EXTERNAL_ENDPOINT_ID, AGRIROUTER_ENDPOINT_ID);
        when(endpointRepository.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).thenReturn(Optional.of(endpoint));
        endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);

        endpointService.updateErrors(endpoint, decodeMessageResponse());

        assertThat(internalEndpointCache.getByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).isEmpty();
        assertThat(internalEndpointCache.get(EXTERNAL_ENDPOINT_ID)).isEmpty();
    }

    @Test
    void updateWarnings_removesTheEndpointFromTheCache() {
        var endpoint = endpoint(EXTERNAL_ENDPOINT_ID, AGRIROUTER_ENDPOINT_ID);
        when(endpointRepository.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).thenReturn(Optional.of(endpoint));
        endpointService.findByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);

        endpointService.updateWarnings(endpoint, decodeMessageResponse());

        assertThat(internalEndpointCache.getByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).isEmpty();
        assertThat(internalEndpointCache.get(EXTERNAL_ENDPOINT_ID)).isEmpty();
    }

    private Endpoint endpoint(String externalEndpointId, String agrirouterEndpointId) {
        var endpoint = new Endpoint();
        endpoint.setExternalEndpointId(externalEndpointId);
        endpoint.setAgrirouterEndpointId(agrirouterEndpointId);
        return endpoint;
    }

    /**
     * An error / warning response as it arrives from the agrirouter©, incl. the details the message is decoded from.
     */
    private DecodeMessageResponse decodeMessageResponse() {
        var decodeMessageResponse = new DecodeMessageResponse();
        decodeMessageResponse.setResponseEnvelope(Response.ResponseEnvelope.newBuilder()
                .setResponseCode(400)
                .setType(Response.ResponseEnvelope.ResponseBodyType.ACK_WITH_FAILURE)
                .build());
        decodeMessageResponse.setResponsePayloadWrapper(Response.ResponsePayloadWrapper.newBuilder()
                .setDetails(Any.getDefaultInstance())
                .build());
        when(decodeMessageService.decode(any(Any.class))).thenReturn(MessageOuterClass.Messages.newBuilder()
                .addMessages(MessageOuterClass.Message.newBuilder()
                        .setMessageCode("VAL_000001")
                        .setMessage("The message is not valid.")
                        .build())
                .build());
        return decodeMessageResponse;
    }
}
