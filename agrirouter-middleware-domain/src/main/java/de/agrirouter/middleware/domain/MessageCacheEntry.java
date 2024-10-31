package de.agrirouter.middleware.domain;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * A message cache entry.
 */
@Getter
@Setter
@Document
@NoArgsConstructor
@AllArgsConstructor
public class MessageCacheEntry {
    private String externalEndpointId;
    private TechnicalMessageType technicalMessageType;
    private List<String> recipients;
    private String filename;
    private ByteString message;
    private String teamSetContextId;
    private Instant createdAt;

    // Expired after two weeks of caching.
    @Indexed(name = "ttl_index", expireAfterSeconds = 60 * 60 * 24 * 14)
    private Instant expiredOn;
}
