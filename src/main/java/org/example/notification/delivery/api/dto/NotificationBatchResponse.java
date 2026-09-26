package org.example.notification.delivery.api.dto;

import java.time.Instant;
import java.util.UUID;
import org.example.notification.delivery.domain.BatchStatus;

public class NotificationBatchResponse {
  private final UUID id;
  private final BatchStatus status;
  private final int totalDeliveries;
  private final long delivered;
  private final long failed;
  private final Instant scheduledAt;
  private final Instant createdAt;

  public NotificationBatchResponse(
      UUID id,
      BatchStatus status,
      int total,
      long delivered,
      long failed,
      Instant scheduledAt,
      Instant createdAt) {
    this.id = id;
    this.status = status;
    this.totalDeliveries = total;
    this.delivered = delivered;
    this.failed = failed;
    this.scheduledAt = scheduledAt;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public BatchStatus getStatus() {
    return status;
  }

  public int getTotalDeliveries() {
    return totalDeliveries;
  }

  public long getDelivered() {
    return delivered;
  }

  public long getFailed() {
    return failed;
  }

  public Instant getScheduledAt() {
    return scheduledAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
