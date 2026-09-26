package org.example.notification.delivery.infrastructure;

import java.util.*;
import org.example.notification.delivery.domain.NotificationBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchRepository extends JpaRepository<NotificationBatch, UUID> {
  Optional<NotificationBatch> findByTenantIdAndIdempotencyKey(UUID tenantId, String key);

  Optional<NotificationBatch> findByIdAndTenantId(UUID id, UUID tenantId);
}
