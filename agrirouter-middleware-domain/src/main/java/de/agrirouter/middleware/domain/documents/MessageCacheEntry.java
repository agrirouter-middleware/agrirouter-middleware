package de.agrirouter.middleware.domain.documents;

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
public class MessageCacheEntry extends NoSqlBaseEntity {
    /**
     * The ID of the endpoint.
     */
    private String externalEndpointId;

    /**
     * The type of the message.
     */
    private TechnicalMessageType technicalMessageType;

    /**
     * The recipients.
     */
    private List<String> recipients;

    /**
     * The filename.
     */
    private String filename;

    /**
     * The message.
     */
    private ByteString message;

    /**
     * The team set context ID.
     */
    private String teamSetContextId;

    /**
     * The timestamp of the cache entry.
     */
    private Instant createdAt;

    /**
     * The date of expiration, two weeks in the future.
     */
    @Indexed(name = "ttl_index", expireAfterSeconds = 60 * 60 * 24 * 14)
    private Instant expiredOn;
}
