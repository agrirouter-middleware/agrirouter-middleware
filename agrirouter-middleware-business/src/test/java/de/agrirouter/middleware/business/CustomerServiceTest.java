package de.agrirouter.middleware.business;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerServiceTest {

    private final CustomerService customerService = new CustomerService(null, null, null);

    @Test
    void extractCustomerIds_realLifeExample_returnsCustomerIds() {
        var realLifeExample = """
                {
                  "customerId": {
                    "uri": ["urn:customer:example:56789"]
                  }
                }""";
        Document document = Document.parse(realLifeExample);
        List<String> customerIds = customerService.extractCustomerIds(document);
        assertThat(customerIds).hasSize(1);
        assertThat(customerIds.get(0)).isEqualTo("urn:customer:example:56789");
    }

    @Test
    void extractCustomerIds_validDocumentWithUris_returnsCustomerIds() {
        Document customerIdDocument = new Document("uri", List.of("customer1", "customer2", "customer3"));
        Document document = new Document("customerId", customerIdDocument);
        List<String> customerIds = customerService.extractCustomerIds(document);
        assertThat(customerIds).hasSize(3);
        assertThat(customerIds).containsExactlyInAnyOrder("customer1", "customer2", "customer3");
    }

    @Test
    void extractCustomerIds_documentWithoutUri_returnsEmptyList() {
        Document customerIdDocument = new Document();
        Document document = new Document("customerId", customerIdDocument);
        List<String> customerIds = customerService.extractCustomerIds(document);
        assertThat(customerIds).isEmpty();
    }

    @Test
    void extractCustomerIds_documentWithoutCustomerId_returnsEmptyList() {
        Document document = new Document();
        List<String> customerIds = customerService.extractCustomerIds(document);
        assertThat(customerIds).isEmpty();
    }

    @Test
    void extractCustomerIds_documentWithInvalidUriType_returnsEmptyList() {
        Document customerIdDocument = new Document("uri", "notAList");
        Document document = new Document("customerId", customerIdDocument);
        List<String> customerIds = customerService.extractCustomerIds(document);
        assertThat(customerIds).isEmpty();
    }

    @Test
    void extractCustomerIds_documentWithNonStringUriElements_returnsEmptyList() {
        Document customerIdDocument = new Document("uri", List.of(123, 456, 789));
        Document document = new Document("customerId", customerIdDocument);
        List<String> customerIds = customerService.extractCustomerIds(document);
        assertThat(customerIds).isEmpty();
    }

    @Test
    void extractCustomerIds_emptyDocument_returnsEmptyList() {
        Document document = new Document();
        List<String> customerIds = customerService.extractCustomerIds(document);
        assertThat(customerIds).isEmpty();
    }

    @Test
    void extractCustomerIds_nullDocument_throwsException() {
        assertThrows(NullPointerException.class, () -> customerService.extractCustomerIds(null));
    }
}