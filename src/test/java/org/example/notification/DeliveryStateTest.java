package org.example.notification;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.example.notification.delivery.domain.*;
import org.example.notification.tenant.domain.ChannelType;
import org.junit.jupiter.api.Test;

class DeliveryStateTest {
  @Test
  void rejectsInvalidTerminalStateTransition() {
    Instant now = Instant.parse("2026-01-01T00:00:00Z");
    Delivery delivery =
        new Delivery(
            null,
            null,
            ChannelType.EMAIL,
            "person@example.test",
            "subject",
            "body",
            DeliveryStatus.QUEUED,
            now,
            "provider-key",
            now);
    delivery.transitionTo(DeliveryStatus.PROCESSING, now);
    delivery.transitionTo(DeliveryStatus.DELIVERED, now);
    assertThrows(
        IllegalStateException.class, () -> delivery.transitionTo(DeliveryStatus.PROCESSING, now));
  }
}
