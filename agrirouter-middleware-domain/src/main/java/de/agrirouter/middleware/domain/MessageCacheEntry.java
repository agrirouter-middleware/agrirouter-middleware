package de.agrirouter.middleware.domain;

import com.dke.data.agrirouter.api.enums.TechnicalMessageType;
import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A message cache entry.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCacheEntry {
    private String externalEndpointId;
    private TechnicalMessageType technicalMessageType;
    private List<String> recipients;
    private String filename;
    private ByteString message;
    private String teamSetContextId;
    private long createdAt;
}
