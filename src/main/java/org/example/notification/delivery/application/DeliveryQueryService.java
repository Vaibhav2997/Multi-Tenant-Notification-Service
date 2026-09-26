package org.example.notification.delivery.application;

import java.time.Instant;
import java.util.*;
import org.example.notification.delivery.api.dto.*;
import org.example.notification.delivery.domain.*;
import org.example.notification.delivery.infrastructure.*;
import org.example.notification.shared.api.NotFoundException;
import org.example.notification.tenant.domain.ChannelType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class DeliveryQueryService {
  private final DeliveryRepository deliveries;
  private final DeliveryAttemptRepository attempts;
  private final DeliveryEventRepository events;

  public DeliveryQueryService(
      DeliveryRepository deliveries,
      DeliveryAttemptRepository attempts,
      DeliveryEventRepository events) {
    this.deliveries = deliveries;
    this.attempts = attempts;
    this.events = events;
  }

  public Page<DeliverySummaryResponse> search(
      UUID tenantId,
      DeliveryStatus status,
      ChannelType channel,
      UUID batchId,
      Instant from,
      Instant to,
      Pageable pageable) {
    Specification<Delivery> filters =
        (root, query, builder) -> builder.equal(root.get("tenant").get("id"), tenantId);
    if (status != null) {
      filters = filters.and((root, query, builder) -> builder.equal(root.get("status"), status));
    }
    if (channel != null) {
      filters = filters.and((root, query, builder) -> builder.equal(root.get("channel"), channel));
    }
    if (batchId != null) {
      filters =
          filters.and(
              (root, query, builder) -> builder.equal(root.get("batch").get("id"), batchId));
    }
    if (from != null) {
      filters =
          filters.and(
              (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("createdAt"), from));
    }
    if (to != null) {
      filters =
          filters.and(
              (root, query, builder) -> builder.lessThanOrEqualTo(root.get("createdAt"), to));
    }
    return deliveries.findAll(filters, pageable).map(this::summary);
  }

  public DeliveryDetailResponse get(UUID tenantId, UUID id) {
    Delivery delivery =
        deliveries
            .findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("Delivery not found"));
    List<DeliveryAttemptResponse> attemptResponses =
        attempts.findByDeliveryIdOrderByAttemptNumber(id).stream()
            .map(
                a ->
                    new DeliveryAttemptResponse(
                        a.getAttemptNumber(),
                        a.getOutcome(),
                        a.getProviderReference(),
                        a.getErrorCode(),
                        a.getErrorMessage(),
                        a.getStartedAt(),
                        a.getCompletedAt()))
            .toList();
    List<DeliveryEventResponse> eventResponses =
        events.findByDeliveryIdOrderByOccurredAt(id).stream()
            .map(
                e ->
                    new DeliveryEventResponse(
                        e.getFromStatus(), e.getToStatus(), e.getReason(), e.getOccurredAt()))
            .toList();
    return new DeliveryDetailResponse(
        summary(delivery),
        delivery.getRenderedSubject(),
        delivery.getRenderedBody(),
        attemptResponses,
        eventResponses);
  }

  private DeliverySummaryResponse summary(Delivery d) {
    return new DeliverySummaryResponse(
        d.getId(),
        d.getBatch().getId(),
        d.getChannel(),
        d.getRecipient(),
        d.getStatus(),
        d.getAttemptCount(),
        d.getNextAttemptAt(),
        d.getCreatedAt());
  }
}
