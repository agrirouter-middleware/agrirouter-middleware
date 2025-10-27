package de.agrirouter.middleware.business;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FieldServiceTest {

    private final FieldService fieldService = new FieldService(null, null, null, null);

    @Test
    void extractUris_realLifeExample_returnsUris() {
        var realLifeExample = """
                {
                  "partfieldId": {
                    "number": "12345",
                    "uri": ["urn:field:example:12345"]
                  }
                }""";
        Document document = Document.parse(realLifeExample);
        List<String> uris = fieldService.extractUris(document);
        assertEquals(1, uris.size());
        assertEquals("urn:field:example:12345", uris.get(0));
    }

    @Test
    void extractUris_validDocumentWithUris_returnsUris() {
        Document uriDocument = new Document("uri", List.of("uri1", "uri2", "uri3"));
        Document partfieldIdDocument = new Document("partfieldId", uriDocument);
        List<String> uris = fieldService.extractUris(partfieldIdDocument);
        assertNotNull(uris, "The list of URIs should not be null.");
        assertEquals(3, uris.size(), "The list of URIs should contain exactly 3 elements.");
        assertTrue(uris.contains("uri1"), "The list of URIs should contain 'uri1'.");
        assertTrue(uris.contains("uri2"), "The list of URIs should contain 'uri2'.");
        assertTrue(uris.contains("uri3"), "The list of URIs should contain 'uri3'.");
    }

    @Test
    void extractUris_documentWithoutUri_returnsEmptyList() {
        Document partfieldIdDocument = new Document("partfieldId", new Document());
        List<String> uris = fieldService.extractUris(partfieldIdDocument);
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty when no 'uri' field exists.");
    }

    /**
     * This test verifies that extractUris returns an empty list when the "partfieldId" field is missing.
     */
    @Test
    void extractUris_documentWithoutPartfieldId_returnsEmptyList() {
        Document documentWithoutPartfieldId = new Document();
        List<String> uris = fieldService.extractUris(documentWithoutPartfieldId);
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty when no 'partfieldId' field exists.");
    }

    @Test
    void extractUris_documentWithNonStringUriElements_returnsEmptyList() {
        // Arrange
        Document uriDocument = new Document("uri", List.of(123, 456, 789)); // Non-string elements
        Document partfieldIdDocument = new Document("partfieldId", uriDocument);
        List<String> uris = fieldService.extractUris(partfieldIdDocument);
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty when the 'uri' field contains non-string elements.");
    }

    @Test
    void extractUris_emptyDocument_returnsEmptyList() {
        Document emptyDocument = new Document();
        List<String> uris = fieldService.extractUris(emptyDocument);
        assertNotNull(uris, "The list of URIs should not be null.");
        assertTrue(uris.isEmpty(), "The list of URIs should be empty for an empty document.");
    }

    @Test
    void parse_validFieldAsJson_returnsPartfield() {
        var fieldAsJson = """
                    {
                         "partfieldCode": "",
                         "partfieldDesignator": "Test1234",
                         "partfieldArea": 702,
                         "polygon": [
                             {
                                 "polygonType": 1,
                                 "polygonDesignator": "",
                                 "polygonArea": 0,
                                 "polygonColour": 0,
                                 "lineString": [
                                     {
                                         "lineStringType": 1,
                                         "lineStringDesignator": "",
                                         "lineStringWidth": 0,
                                         "lineStringLength": 0,
                                         "lineStringColour": 0,
                                         "point": [
                                             {
                                                 "pointType": 2,
                                                 "pointDesignator": "",
                                                 "pointNorth": 52.063077,
                                                 "pointEast": 13.857652,
                                                 "pointUp": 0,
                                                 "pointColour": 0,
                                                 "pointHorizontalAccuracy": 0,
                                                 "pointVerticalAccuracy": 0,
                                                 "filename": "",
                                                 "filelength": 0,
                                                 "extension": []
                                             },
                                             {
                                                 "pointType": 2,
                                                 "pointDesignator": "",
                                                 "pointNorth": 52.062947,
                                                 "pointEast": 13.858121,
                                                 "pointUp": 0,
                                                 "pointColour": 0,
                                                 "pointHorizontalAccuracy": 0,
                                                 "pointVerticalAccuracy": 0,
                                                 "filename": "",
                                                 "filelength": 0,
                                                 "extension": []
                                             },
                                             {
                                                 "pointType": 2,
                                                 "pointDesignator": "",
                                                 "pointNorth": 52.062703,
                                                 "pointEast": 13.857586,
                                                 "pointUp": 0,
                                                 "pointColour": 0,
                                                 "pointHorizontalAccuracy": 0,
                                                 "pointVerticalAccuracy": 0,
                                                 "filename": "",
                                                 "filelength": 0,
                                                 "extension": []
                                             },
                                             {
                                                 "pointType": 2,
                                                 "pointDesignator": "",
                                                 "pointNorth": 52.063077,
                                                 "pointEast": 13.857652,
                                                 "pointUp": 0,
                                                 "pointColour": 0,
                                                 "pointHorizontalAccuracy": 0,
                                                 "pointVerticalAccuracy": 0,
                                                 "filename": "",
                                                 "filelength": 0,
                                                 "extension": []
                                             }
                                         ],
                                         "extension": []
                                     }
                                 ],
                                 "extension": []
                             }
                         ],
                         "lineString": [],
                         "point": [],
                         "guidanceGroup": [],
                         "extension": [],
                         "partfieldId": {
                             "number": 0,
                             "uri": [
                                 "{bd94682f-b1ef-4ffc-8154-c0b63c435d80}"
                             ]
                         },
                         "customerIdRef": {
                             "number": 0,
                             "uri": [
                                 "{9cc2c5c6-2029-4dca-b088-63327a8ae6da}"
                             ]
                         }
                     }
                """;
        var result = fieldService.parse(fieldAsJson);
        assertTrue(result.isPresent());
        var field = result.get();
        assertEquals("Test1234", field.getPartfieldDesignator(), "Field designator should match");
        assertEquals(702, field.getPartfieldArea(), "Field area should match");
    }

}