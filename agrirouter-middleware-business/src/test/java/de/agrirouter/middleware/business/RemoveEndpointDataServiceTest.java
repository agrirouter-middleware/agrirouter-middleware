package de.agrirouter.middleware.business;

import de.agrirouter.middleware.business.cache.endpoints.InternalEndpointCache;
import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.persistence.jpa.ContentMessageRepository;
import de.agrirouter.middleware.persistence.jpa.EndpointRepository;
import de.agrirouter.middleware.persistence.jpa.UnprocessedMessageRepository;
import de.agrirouter.middleware.persistence.mongo.DeviceDescriptionRepository;
import de.agrirouter.middleware.persistence.mongo.TimeLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RemoveEndpointDataServiceTest {

    private static final String AGRIROUTER_ENDPOINT_ID = "ar-endpoint-id";
    private static final String EXTERNAL_ENDPOINT_ID = "external-endpoint-id";

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private UnprocessedMessageRepository unprocessedMessageRepository;

    @Mock
    private ContentMessageRepository contentMessageRepository;

    @Mock
    private DeviceDescriptionRepository deviceDescriptionRepository;

    @Mock
    private TimeLogRepository timeLogRepository;

    @Spy
    private InternalEndpointCache internalEndpointCache = new InternalEndpointCache();

    @InjectMocks
    private RemoveEndpointDataService removeEndpointDataService;

    @Test
    void removeEndpointData_removesTheEndpointFromBothCacheIndices() {
        var endpoint = cachedEndpoint();

        removeEndpointDataService.removeEndpointData(endpoint);

        assertThat(internalEndpointCache.get(EXTERNAL_ENDPOINT_ID)).isEmpty();
        assertThat(internalEndpointCache.getByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).isEmpty();
    }

    @Test
    void removeEndpointDataAndEndpoint_removesTheEndpointFromBothCacheIndices() {
        var endpoint = cachedEndpoint();

        removeEndpointDataService.removeEndpointDataAndEndpoint(endpoint);

        verify(endpointRepository).delete(endpoint);
        assertThat(internalEndpointCache.get(EXTERNAL_ENDPOINT_ID)).isEmpty();
        assertThat(internalEndpointCache.getByAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID)).isEmpty();
    }

    private Endpoint cachedEndpoint() {
        var endpoint = new Endpoint();
        endpoint.setExternalEndpointId(EXTERNAL_ENDPOINT_ID);
        endpoint.setAgrirouterEndpointId(AGRIROUTER_ENDPOINT_ID);
        internalEndpointCache.put(EXTERNAL_ENDPOINT_ID, endpoint);
        return endpoint;
    }
}
