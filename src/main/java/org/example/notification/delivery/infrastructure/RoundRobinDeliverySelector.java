package org.example.notification.delivery.infrastructure;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RoundRobinDeliverySelector {
  public List<Selection> select(Map<UUID, Deque<UUID>> tenantQueues, int maximum) {
    List<Selection> result = new ArrayList<>();
    boolean remaining = true;
    while (remaining && result.size() < maximum) {
      remaining = false;
      for (Map.Entry<UUID, Deque<UUID>> entry : tenantQueues.entrySet()) {
        UUID deliveryId = entry.getValue().pollFirst();
        if (deliveryId != null) {
          remaining = true;
          result.add(new Selection(entry.getKey(), deliveryId));
          if (result.size() == maximum) break;
        }
      }
    }
    return result;
  }

  public static final class Selection {
    private final UUID tenantId;
    private final UUID deliveryId;

    public Selection(UUID tenantId, UUID deliveryId) {
      this.tenantId = tenantId;
      this.deliveryId = deliveryId;
    }

    public UUID getTenantId() {
      return tenantId;
    }

    public UUID getDeliveryId() {
      return deliveryId;
    }
  }
}
