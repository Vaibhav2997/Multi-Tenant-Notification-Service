package org.example.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.example.notification.delivery.infrastructure.RoundRobinDeliverySelector;
import org.junit.jupiter.api.Test;

class RoundRobinDeliverySelectorTest {
  @Test
  void alternatesTenantsWhileBothHaveWork() {
    UUID tenantA = UUID.randomUUID();
    UUID tenantB = UUID.randomUUID();
    Map<UUID, Deque<UUID>> queues = new LinkedHashMap<>();
    queues.put(tenantA, new ArrayDeque<>(List.of(UUID.randomUUID(), UUID.randomUUID())));
    queues.put(tenantB, new ArrayDeque<>(List.of(UUID.randomUUID(), UUID.randomUUID())));

    List<RoundRobinDeliverySelector.Selection> result =
        new RoundRobinDeliverySelector().select(queues, 4);

    assertEquals(
        List.of(tenantA, tenantB, tenantA, tenantB),
        result.stream().map(RoundRobinDeliverySelector.Selection::getTenantId).toList());
  }
}
