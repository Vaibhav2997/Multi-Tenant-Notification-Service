package org.example.notification.delivery.infrastructure;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
import org.example.notification.delivery.domain.*;
import org.example.notification.tenant.domain.ChannelType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository
    extends JpaRepository<Delivery, UUID>, JpaSpecificationExecutor<Delivery> {
  @Query(
      "select distinct d.tenant.id from Delivery d where d.status in :statuses and d.nextAttemptAt <= :now")
  List<UUID> findDueTenantIds(
      @Param("statuses") Collection<DeliveryStatus> statuses, @Param("now") Instant now);

  @Query(
      "select d from Delivery d where d.tenant.id=:tenantId and d.status in :statuses and d.nextAttemptAt<=:now order by d.nextAttemptAt,d.createdAt")
  List<Delivery> findDueForTenant(
      UUID tenantId, Collection<DeliveryStatus> statuses, Instant now, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select d from Delivery d join fetch d.tenant join fetch d.batch where d.id=:id")
  Optional<Delivery> lockById(UUID id);

  Optional<Delivery> findByIdAndTenantId(UUID id, UUID tenantId);

  Page<Delivery> findByTenantId(UUID tenantId, Pageable pageable);

  Page<Delivery> findByTenantIdAndStatus(UUID tenantId, DeliveryStatus status, Pageable pageable);

  Page<Delivery> findByTenantIdAndChannel(UUID tenantId, ChannelType channel, Pageable pageable);

  List<Delivery> findByBatchId(UUID batchId);
}
