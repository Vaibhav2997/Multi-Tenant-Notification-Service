package org.example.notification.delivery.infrastructure;

import java.util.*;
import org.example.notification.delivery.domain.DeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryEventRepository extends JpaRepository<DeliveryEvent, UUID> {
  List<DeliveryEvent> findByDeliveryIdOrderByOccurredAt(UUID deliveryId);
}
