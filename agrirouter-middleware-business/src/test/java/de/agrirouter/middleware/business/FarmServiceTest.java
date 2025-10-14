package de.agrirouter.middleware.business;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FarmServiceTest {

    private final FarmService farmService = new FarmService(null, null, null);

    @Test
    void extractUris_realLifeExample_returnsUris() {
        var realLifeExampple = """
                {
                  "farmId": {
                    "number": "12345",
                    "uri": ["urn:farm:example:12345"]
                  }
                }""";
        Document document = Document.parse(realLifeExampple);
        List<String> uris = farmService.extractUris(document);
        assertEquals(1, uris.size());
        assertEquals("urn:farm:example:12345", uris.get(0));
    }

    @Test
    void givenDocumentWithSingleUri_whenExtractUris_thenReturnSingleUri() {
        var document = new Document("farmId", new Document("uri", "uriValue1"));
        var result = farmService.extractUris(document);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("uriValue1"));
    }

    @Test
    void givenDocumentWithMultipleUris_whenExtractUris_thenReturnAllUris() {
        var uris = List.of("uriValue1", "uriValue2");
        var document = new Document("farmId", new Document("uri", uris));
        var result = farmService.extractUris(document);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(uris));
    }

    @Test
    void givenDocumentWithoutUris_whenExtractUris_thenReturnEmptyList() {
        var document = new Document("farmId", new Document());
        var result = farmService.extractUris(document);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenDocumentWithoutFarmId_whenExtractUris_thenReturnEmptyList() {
        var document = new Document("someOtherKey", new Document());
        var result = farmService.extractUris(document);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenDocumentWithInvalidUriType_whenExtractUris_thenReturnEmptyList() {
        var document = new Document("farmId", new Document("uri", 12345));
        var result = farmService.extractUris(document);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenDocumentWithMixedUriTypes_whenExtractUris_thenReturnValidUris() {
        var uris = List.of("uriValue1", 12345, "uriValue2");
        var document = new Document("farmId", new Document("uri", uris));
        var result = farmService.extractUris(document);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("uriValue1"));
        assertTrue(result.contains("uriValue2"));
    }
}