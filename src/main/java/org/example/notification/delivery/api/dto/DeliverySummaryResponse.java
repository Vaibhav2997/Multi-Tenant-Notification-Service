package org.example.notification.delivery.api.dto;

import java.time.Instant;
import java.util.UUID;
import org.example.notification.delivery.domain.DeliveryStatus;
import org.example.notification.tenant.domain.ChannelType;

public class DeliverySummaryResponse {
  private final UUID id, batchId;
  private final ChannelType channel;
  private final String recipient;
  private final DeliveryStatus status;
  private final int attemptCount;
  private final Instant nextAttemptAt, createdAt;

  public DeliverySummaryResponse(
      UUID id,
      UUID batchId,
      ChannelType channel,
      String recipient,
      DeliveryStatus status,
      int attempts,
      Instant next,
      Instant created) {
    this.id = id;
    this.batchId = batchId;
    this.channel = channel;
    this.recipient = recipient;
    this.status = status;
    this.attemptCount = attempts;
    this.nextAttemptAt = next;
    this.createdAt = created;
  }

  public UUID getId() {
    return id;
  }

  public UUID getBatchId() {
    return batchId;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public String getRecipient() {
    return recipient;
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

  public Instant getCreatedAt() {
    return createdAt;
  }
}
