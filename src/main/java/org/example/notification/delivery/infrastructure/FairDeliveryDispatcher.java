package org.example.notification.delivery.infrastructure;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.notification.delivery.application.DeliveryProcessor;
import org.example.notification.delivery.domain.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class FairDeliveryDispatcher {
  private static final List<DeliveryStatus> ELIGIBLE =
      List.of(DeliveryStatus.SCHEDULED, DeliveryStatus.QUEUED, DeliveryStatus.RETRY_SCHEDULED);
  private final DeliveryRepository deliveries;
  private final DeliveryProcessor processor;
  private final ThreadPoolTaskExecutor executor;
  private final RoundRobinDeliverySelector selector;
  private final Clock clock;
  private final int tenantBurst;
  private final Set<UUID> scheduled = ConcurrentHashMap.newKeySet();
  private final AtomicInteger cursor = new AtomicInteger();

  public FairDeliveryDispatcher(
      DeliveryRepository deliveries,
      DeliveryProcessor processor,
      @Qualifier("notificationExecutor") ThreadPoolTaskExecutor executor,
      RoundRobinDeliverySelector selector,
      Clock clock,
      @Value("${notification.dispatch.tenant-burst}") int tenantBurst) {
    this.deliveries = deliveries;
    this.processor = processor;
    this.executor = executor;
    this.selector = selector;
    this.clock = clock;
    this.tenantBurst = tenantBurst;
  }

  @Scheduled(fixedDelayString = "${notification.dispatch.poll-ms}")
  public void dispatch() {
    List<UUID> tenants = new ArrayList<>(deliveries.findDueTenantIds(ELIGIBLE, clock.instant()));
    if (tenants.isEmpty()) return;
    Collections.sort(tenants);
    Collections.rotate(tenants, -Math.floorMod(cursor.getAndIncrement(), tenants.size()));
    Map<UUID, Deque<UUID>> queues = new LinkedHashMap<>();
    for (UUID tenantId : tenants) {
      Deque<UUID> ids = new ArrayDeque<>();
      deliveries
          .findDueForTenant(tenantId, ELIGIBLE, clock.instant(), PageRequest.of(0, tenantBurst))
          .forEach(d -> ids.add(d.getId()));
      queues.put(tenantId, ids);
    }
    int capacity = availableCapacity();
    for (RoundRobinDeliverySelector.Selection selection : selector.select(queues, capacity)) {
      submit(selection.getDeliveryId(), selection.getTenantId());
    }
  }

  private int availableCapacity() {
    ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
    int idleWorkers = Math.max(0, pool.getMaximumPoolSize() - pool.getActiveCount());
    return idleWorkers + pool.getQueue().remainingCapacity();
  }

  private void submit(UUID deliveryId, UUID tenantId) {
    if (!scheduled.add(deliveryId)) return;
    try {
      executor.execute(
          () -> {
            try {
              processor.process(deliveryId, tenantId);
            } finally {
              scheduled.remove(deliveryId);
            }
          });
    } catch (RejectedExecutionException exception) {
      scheduled.remove(deliveryId);
    }
  }
}
