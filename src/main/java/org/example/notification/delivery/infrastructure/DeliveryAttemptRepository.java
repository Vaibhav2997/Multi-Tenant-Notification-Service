package org.example.notification.delivery.infrastructure;

import java.util.*;
import org.example.notification.delivery.domain.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {
  List<DeliveryAttempt> findByDeliveryIdOrderByAttemptNumber(UUID deliveryId);

  Optional<DeliveryAttempt> findByDeliveryIdAndAttemptNumber(UUID deliveryId, int number);
}
