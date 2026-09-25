package de.agrirouter.middleware.business;

import de.agrirouter.middleware.domain.Endpoint;
import de.agrirouter.middleware.persistence.jpa.EndpointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndpointServiceTest {

    private static final long TIME_TO_REMOVE_AN_ENDPOINT_IN_MILLISECONDS = 100;
    private static final long MAXIMUM_TIME_TO_WAIT_IN_MILLISECONDS = 5_000;

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private RemoveEndpointDataService removeEndpointDataService;

    @InjectMocks
    private EndpointService endpointService;

    @Test
    void deleteWithoutMatchingEndpointsShouldNotFail() {
        var externalEndpointId = "external-endpoint-id";
        when(endpointRepository.findAllByExternalEndpointId(externalEndpointId)).thenReturn(Collections.emptyList());

        assertThatCode(() -> endpointService.delete(externalEndpointId)).doesNotThrowAnyException();

        verifyNoInteractions(removeEndpointDataService);
    }

    @Test
    void deleteShouldRemoveTheDataForEachDeletedEndpoint() {
        var externalEndpointId = "external-endpoint-id";
        var firstEndpoint = endpoint("agrirouter-endpoint-id-1", externalEndpointId);
        var secondEndpoint = endpoint("agrirouter-endpoint-id-2", externalEndpointId);
        when(endpointRepository.findAllByExternalEndpointId(externalEndpointId)).thenReturn(List.of(firstEndpoint, secondEndpoint));
        takeSomeTimeWhenRemovingTheEndpoint();

        endpointService.delete(externalEndpointId);

        verify(removeEndpointDataService).removeEndpointDataAndEndpoint(firstEndpoint);
        verify(removeEndpointDataService).removeEndpointDataAndEndpoint(secondEndpoint);
        verify(removeEndpointDataService).removeData("agrirouter-endpoint-id-1");
        verify(removeEndpointDataService).removeData("agrirouter-endpoint-id-2");
    }

    @Test
    void deleteShouldRemoveTheDataForTheConnectedVirtualEndpointsAsWell() {
        var externalEndpointId = "external-endpoint-id";
        var virtualEndpoint = endpoint("agrirouter-endpoint-id-virtual", "external-endpoint-id-virtual");
        var mainEndpoint = endpoint("agrirouter-endpoint-id-main", externalEndpointId);
        mainEndpoint.setConnectedVirtualEndpoints(List.of(virtualEndpoint));
        when(endpointRepository.findAllByExternalEndpointId(externalEndpointId)).thenReturn(List.of(mainEndpoint));
        takeSomeTimeWhenRemovingTheEndpoint();

        endpointService.delete(externalEndpointId);

        verify(removeEndpointDataService).removeEndpointData(virtualEndpoint);
        verify(removeEndpointDataService).removeEndpointDataAndEndpoint(mainEndpoint);
        verify(removeEndpointDataService).removeData("agrirouter-endpoint-id-virtual");
        verify(removeEndpointDataService).removeData("agrirouter-endpoint-id-main");
    }

    @Test
    void deleteShouldNotLeaveAnyRunningThreadsBehind() throws InterruptedException {
        var externalEndpointId = "external-endpoint-id";
        var endpoints = List.of(endpoint("agrirouter-endpoint-id-1", externalEndpointId),
                endpoint("agrirouter-endpoint-id-2", externalEndpointId));
        when(endpointRepository.findAllByExternalEndpointId(externalEndpointId)).thenReturn(endpoints);
        var threadsUsedForTheRemoval = new ConcurrentLinkedQueue<Thread>();
        doAnswer(invocation -> {
            threadsUsedForTheRemoval.add(Thread.currentThread());
            return null;
        }).when(removeEndpointDataService).removeEndpointDataAndEndpoint(org.mockito.ArgumentMatchers.any());

        endpointService.delete(externalEndpointId);

        awaitUntilAllThreadsAreTerminated(threadsUsedForTheRemoval, endpoints.size());
        assertThat(threadsUsedForTheRemoval).hasSize(endpoints.size());
        assertThat(threadsUsedForTheRemoval).noneMatch(Thread::isAlive);
    }

    private void awaitUntilAllThreadsAreTerminated(Collection<Thread> threads, int expectedNumberOfThreads) throws InterruptedException {
        var deadline = System.currentTimeMillis() + MAXIMUM_TIME_TO_WAIT_IN_MILLISECONDS;
        while (System.currentTimeMillis() < deadline
                && (threads.size() < expectedNumberOfThreads || threads.stream().anyMatch(Thread::isAlive))) {
            Thread.sleep(25);
        }
    }

    private void takeSomeTimeWhenRemovingTheEndpoint() {
        doAnswer(invocation -> {
            Thread.sleep(TIME_TO_REMOVE_AN_ENDPOINT_IN_MILLISECONDS);
            return null;
        }).when(removeEndpointDataService).removeEndpointDataAndEndpoint(org.mockito.ArgumentMatchers.any());
    }

    private Endpoint endpoint(String agrirouterEndpointId, String externalEndpointId) {
        var endpoint = new Endpoint();
        endpoint.setAgrirouterEndpointId(agrirouterEndpointId);
        endpoint.setExternalEndpointId(externalEndpointId);
        return endpoint;
    }

}
