package de.agrirouter.middleware.business.cache.messaging;

import com.dke.data.agrirouter.api.enums.ContentMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cache for messages.
 */
@Slf4j
@Component
@Profile("persistent-message-cache")
public class PersistentMessageCache implements MessageCache {

    @Override
    public void put(String externalEndpointId, String base64EncodedMessageContent, String filename, List<String> recipients, ContentMessageType contentMessageType) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void sendMessages() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
