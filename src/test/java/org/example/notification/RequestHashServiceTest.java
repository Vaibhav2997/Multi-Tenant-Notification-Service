package org.example.notification;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.example.notification.delivery.api.dto.*;
import org.example.notification.delivery.application.RequestHashService;
import org.junit.jupiter.api.Test;

class RequestHashServiceTest {
  private final RequestHashService hashes = new RequestHashService();

  @Test
  void mapOrderDoesNotChangeCanonicalRequestHash() {
    UUID templateId = UUID.randomUUID();
    CreateNotificationBatchRequest first = request(templateId, Map.of("a", "1", "b", "2"));
    Map<String, String> reversed = new LinkedHashMap<>();
    reversed.put("b", "2");
    reversed.put("a", "1");
    CreateNotificationBatchRequest second = request(templateId, reversed);
    assertEquals(hashes.hash(first), hashes.hash(second));
  }

  @Test
  void recipientChangeProducesDifferentHash() {
    UUID templateId = UUID.randomUUID();
    CreateNotificationBatchRequest first = request(templateId, Map.of("name", "Ada"));
    CreateNotificationBatchRequest second = request(templateId, Map.of("name", "Grace"));
    assertNotEquals(hashes.hash(first), hashes.hash(second));
  }

  private CreateNotificationBatchRequest request(UUID templateId, Map<String, String> variables) {
    RecipientRequest recipient = new RecipientRequest();
    recipient.setAddress("person@example.test");
    recipient.setVariables(variables);
    CreateNotificationBatchRequest request = new CreateNotificationBatchRequest();
    request.setTemplateId(templateId);
    request.setRecipients(List.of(recipient));
    return request;
  }
}
