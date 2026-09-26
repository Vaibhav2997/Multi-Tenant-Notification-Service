package org.example.notification.delivery.application;

import jakarta.transaction.Transactional;
import java.time.*;
import java.util.*;
import org.example.notification.delivery.domain.*;
import org.example.notification.delivery.infrastructure.*;
import org.example.notification.tenant.application.PlatformAdministrationService;
import org.example.notification.tenant.domain.PlatformPolicy;
import org.springframework.stereotype.Service;

@Service
public class DeliveryStateService {
  private final DeliveryRepository deliveries;
  private final DeliveryAttemptRepository attempts;
  private final DeliveryEventRepository events;
  private final BatchRepository batches;
  private final PlatformAdministrationService platform;
  private final RetryDelayCalculator retryDelayCalculator;
  private final Clock clock;

  public DeliveryStateService(
      DeliveryRepository deliveries,
      DeliveryAttemptRepository attempts,
      DeliveryEventRepository events,
      BatchRepository batches,
      PlatformAdministrationService platform,
      RetryDelayCalculator retryDelayCalculator,
      Clock clock) {
    this.deliveries = deliveries;
    this.attempts = attempts;
    this.events = events;
    this.batches = batches;
    this.platform = platform;
    this.retryDelayCalculator = retryDelayCalculator;
    this.clock = clock;
  }

  @Transactional
  public DeliveryCommand claim(UUID deliveryId) {
    Delivery delivery = deliveries.lockById(deliveryId).orElse(null);
    if (delivery == null || delivery.getNextAttemptAt().isAfter(clock.instant())) return null;
    if (delivery.getStatus() == DeliveryStatus.SCHEDULED)
      transition(delivery, DeliveryStatus.QUEUED, "Scheduled time reached");
    else if (delivery.getStatus() == DeliveryStatus.RETRY_SCHEDULED)
      transition(delivery, DeliveryStatus.QUEUED, "Retry delay elapsed");
    if (delivery.getStatus() != DeliveryStatus.QUEUED) return null;
    transition(delivery, DeliveryStatus.PROCESSING, "Claimed by delivery worker");
    delivery.incrementAttempt();
    attempts.save(new DeliveryAttempt(delivery, delivery.getAttemptCount(), clock.instant()));
    delivery.getBatch().changeStatus(BatchStatus.PROCESSING, clock.instant());
    return new DeliveryCommand(
        delivery.getId(),
        delivery.getTenant().getId(),
        delivery.getAttemptCount(),
        delivery.getChannel(),
        delivery.getRecipient(),
        delivery.getRenderedSubject(),
        delivery.getRenderedBody(),
        delivery.getProviderIdempotencyKey());
  }

  @Transactional
  public void deferRateLimited(UUID deliveryId) {
    Delivery delivery = deliveries.lockById(deliveryId).orElse(null);
    if (delivery != null
        && (delivery.getStatus() == DeliveryStatus.QUEUED
            || delivery.getStatus() == DeliveryStatus.RETRY_SCHEDULED
            || delivery.getStatus() == DeliveryStatus.SCHEDULED))
      delivery.scheduleNextAttempt(clock.instant().plusSeconds(1));
  }

  @Transactional
  public void complete(UUID deliveryId, ProviderResult result) {
    Delivery delivery = deliveries.lockById(deliveryId).orElseThrow();
    if (delivery.getStatus() != DeliveryStatus.PROCESSING) return;
    DeliveryAttempt attempt =
        attempts
            .findByDeliveryIdAndAttemptNumber(deliveryId, delivery.getAttemptCount())
            .orElseThrow();
    if (result.getOutcome() == ProviderOutcome.SUCCESS) {
      attempt.complete(
          AttemptOutcome.DELIVERED, result.getProviderReference(), null, null, clock.instant());
      transition(delivery, DeliveryStatus.DELIVERED, "Provider accepted notification");
    } else if (result.getOutcome() == ProviderOutcome.PERMANENT_FAILURE) {
      attempt.complete(
          AttemptOutcome.PERMANENT_FAILURE,
          null,
          result.getErrorCode(),
          result.getMessage(),
          clock.instant());
      transition(delivery, DeliveryStatus.FAILED, "Permanent provider failure");
    } else {
      attempt.complete(
          AttemptOutcome.TRANSIENT_FAILURE,
          null,
          result.getErrorCode(),
          result.getMessage(),
          clock.instant());
      PlatformPolicy policy = platform.requirePolicy();
      if (delivery.getAttemptCount() >= policy.getMaxAttempts())
        transition(delivery, DeliveryStatus.FAILED, "Retry attempts exhausted");
      else {
        transition(delivery, DeliveryStatus.RETRY_SCHEDULED, "Transient provider failure");
        long delay =
            retryDelayCalculator.calculateSeconds(
                delivery.getAttemptCount(),
                policy.getBaseRetryDelaySeconds(),
                policy.getMaxRetryDelaySeconds());
        delivery.scheduleNextAttempt(clock.instant().plusSeconds(delay));
      }
    }
    refreshBatch(delivery.getBatch());
  }

  private void transition(Delivery delivery, DeliveryStatus target, String reason) {
    DeliveryStatus from = delivery.getStatus();
    delivery.transitionTo(target, clock.instant());
    events.save(new DeliveryEvent(delivery, from, target, reason, clock.instant()));
  }

  private void refreshBatch(NotificationBatch batch) {
    List<Delivery> all = deliveries.findByBatchId(batch.getId());
    if (all.stream()
        .anyMatch(
            d ->
                d.getStatus() != DeliveryStatus.DELIVERED
                    && d.getStatus() != DeliveryStatus.FAILED)) return;
    long delivered = all.stream().filter(d -> d.getStatus() == DeliveryStatus.DELIVERED).count();
    BatchStatus status =
        delivered == all.size()
            ? BatchStatus.COMPLETED
            : delivered == 0 ? BatchStatus.FAILED : BatchStatus.PARTIALLY_FAILED;
    batch.changeStatus(status, clock.instant());
    batches.save(batch);
  }
}
