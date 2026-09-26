package org.example.notification.delivery.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.example.notification.tenant.domain.*;

@Entity
@Table(name = "deliveries")
public class Delivery {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Tenant tenant;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private NotificationBatch batch;

  @Enumerated(EnumType.STRING)
  private ChannelType channel;

  private String recipient;
  private String renderedSubject;

  @Column(columnDefinition = "text")
  private String renderedBody;

  @Enumerated(EnumType.STRING)
  private DeliveryStatus status;

  private int attemptCount;
  private Instant nextAttemptAt;
  private String providerIdempotencyKey;
  private Instant createdAt;
  private Instant updatedAt;
  private Instant completedAt;

  protected Delivery() {}

  public Delivery(
      Tenant tenant,
      NotificationBatch batch,
      ChannelType channel,
      String recipient,
      String subject,
      String body,
      DeliveryStatus status,
      Instant nextAttemptAt,
      String providerKey,
      Instant now) {
    this.tenant = tenant;
    this.batch = batch;
    this.channel = channel;
    this.recipient = recipient;
    this.renderedSubject = subject;
    this.renderedBody = body;
    this.status = status;
    this.nextAttemptAt = nextAttemptAt;
    this.providerIdempotencyKey = providerKey;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void transitionTo(DeliveryStatus target, Instant now) {
    if (!canTransition(status, target))
      throw new IllegalStateException("Invalid delivery transition: " + status + " -> " + target);
    status = target;
    updatedAt = now;
    if (target == DeliveryStatus.DELIVERED || target == DeliveryStatus.FAILED) completedAt = now;
  }

  public void incrementAttempt() {
    attemptCount++;
  }

  public void scheduleNextAttempt(Instant time) {
    nextAttemptAt = time;
  }

  private boolean canTransition(DeliveryStatus from, DeliveryStatus to) {
    return switch (from) {
      case SCHEDULED -> to == DeliveryStatus.QUEUED;
      case QUEUED -> to == DeliveryStatus.PROCESSING;
      case PROCESSING ->
          to == DeliveryStatus.DELIVERED
              || to == DeliveryStatus.RETRY_SCHEDULED
              || to == DeliveryStatus.FAILED;
      case RETRY_SCHEDULED -> to == DeliveryStatus.QUEUED;
      case DELIVERED, FAILED -> false;
    };
  }

  public UUID getId() {
    return id;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public NotificationBatch getBatch() {
    return batch;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public String getRecipient() {
    return recipient;
  }

  public String getRenderedSubject() {
    return renderedSubject;
  }

  public String getRenderedBody() {
    return renderedBody;
  }

  public DeliveryStatus getStatus() {
    return status;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public Instant getNextAttemptAt() {
    return nextAttemptAt;
  }

  public String getProviderIdempotencyKey() {
    return providerIdempotencyKey;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }
}
