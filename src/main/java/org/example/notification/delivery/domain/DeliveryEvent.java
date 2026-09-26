package org.example.notification.delivery.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_events")
public class DeliveryEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Delivery delivery;

  @Enumerated(EnumType.STRING)
  private DeliveryStatus fromStatus;

  @Enumerated(EnumType.STRING)
  private DeliveryStatus toStatus;

  private String reason;
  private Instant occurredAt;

  protected DeliveryEvent() {}

  public DeliveryEvent(
      Delivery delivery, DeliveryStatus from, DeliveryStatus to, String reason, Instant now) {
    this.delivery = delivery;
    this.fromStatus = from;
    this.toStatus = to;
    this.reason = reason;
    this.occurredAt = now;
  }

  public UUID getId() {
    return id;
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
