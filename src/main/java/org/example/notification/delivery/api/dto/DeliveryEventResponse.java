package org.example.notification.delivery.api.dto;

import java.time.Instant;
import org.example.notification.delivery.domain.DeliveryStatus;

public class DeliveryEventResponse {
  private final DeliveryStatus fromStatus, toStatus;
  private final String reason;
  private final Instant occurredAt;

  public DeliveryEventResponse(DeliveryStatus from, DeliveryStatus to, String reason, Instant at) {
    fromStatus = from;
    toStatus = to;
    this.reason = reason;
    occurredAt = at;
  }

  public DeliveryStatus getFromStatus() {
    return fromStatus;
  }

  public DeliveryStatus getToStatus() {
    return toStatus;
  }

  public String getReason() {
    return reason;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }
}
