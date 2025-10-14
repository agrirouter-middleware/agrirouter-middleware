package de.agrirouter.middleware.business;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FieldServiceTest {

    private final FieldService fieldService = new FieldService(null, null, null);

    @Test
    void extractUris_realLifeExample_returnsUris() {
        var realLifeExampple = "{\n" +
                "  \"partfieldId\": {\n" +
                "    \"number\": \"12345\",\n" +
                "    \"uri\": [\"urn:field:example:12345\"]\n" +
                "  }\n" +
                "}";
        Document document = Document.parse(realLifeExampple);
        List<String> uris = fieldService.extractUris(document);
        assertEquals(1, uris.size());
        assertEquals("urn:field:example:12345", uris.get(0));
    }

    /**
     * This test verifies that extractUris extracts URIs correctly when the document contains a valid structure.
     */
    @Test
    void extractUris_validDocumentWithUris_returnsUris() {
        // Arrange
        Document uriDocument = new Document("uri", List.of("uri1", "uri2", "uri3"));
        Document partfieldIdDocument = new Document("partfieldId", uriDocument);

        // Act
        List<String> uris = fieldService.extractUris(partfieldIdDocument);

        // Assert
        assertNotNull(uris, "The list of URIs should not be null.");
        assertEquals(3, uris.size(), "The list of URIs should contain exactly 3 elements.");
        assertTrue(uris.contains("uri1"), "The list of URIs should contain 'uri1'.");
        assertTrue(uris.contains("uri2"), "The list of URIs should contain 'uri2'.");
        assertTrue(uris.contains("uri3"), "The list of URIs should contain 'uri3'.");
    }

    /**
     * This test verifies that extractUris returns an empty list when the "uri" field is missing.
     */
    @Test
    void extractUris_documentWithoutUri_returnsEmptyList() {
        // Arrange
        Document partfieldIdDocument = new Document("partfieldId", new Document());

        // Act
        List<String> uris = fieldService.extractUris(partfieldIdDocument);

        // Assert
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty when no 'uri' field exists.");
    }

    /**
     * This test verifies that extractUris returns an empty list when the "partfieldId" field is missing.
     */
    @Test
    void extractUris_documentWithoutPartfieldId_returnsEmptyList() {
        // Arrange
        Document documentWithoutPartfieldId = new Document();

        // Act
        List<String> uris = fieldService.extractUris(documentWithoutPartfieldId);

        // Assert
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty when no 'partfieldId' field exists.");
    }

    /**
     * This test verifies that extractUris returns an empty list when the "uri" field is present but not a list.
     */
    @Test
    void extractUris_documentWithInvalidUriType_returnsEmptyList() {
        // Arrange
        Document invalidUriDocument = new Document("uri", "notAList");
        Document partfieldIdDocument = new Document("partfieldId", invalidUriDocument);

        // Act
        List<String> uris = fieldService.extractUris(partfieldIdDocument);

        // Assert
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty when the 'uri' field is not a list.");
    }

    /**
     * This test verifies that extractUris returns an empty list when the "uri" field contains non-string elements.
     */
    @Test
    void extractUris_documentWithNonStringUriElements_returnsEmptyList() {
        // Arrange
        Document uriDocument = new Document("uri", List.of(123, 456, 789)); // Non-string elements
        Document partfieldIdDocument = new Document("partfieldId", uriDocument);

        // Act
        List<String> uris = fieldService.extractUris(partfieldIdDocument);

        // Assert
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty when the 'uri' field contains non-string elements.");
    }

    /**
     * This test verifies that extractUris handles an empty document without throwing exceptions.
     */
    @Test
    void extractUris_emptyDocument_returnsEmptyList() {
        // Arrange
        Document emptyDocument = new Document();

        // Act
        List<String> uris = fieldService.extractUris(emptyDocument);

        // Assert
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty for an empty document.");
    }
}