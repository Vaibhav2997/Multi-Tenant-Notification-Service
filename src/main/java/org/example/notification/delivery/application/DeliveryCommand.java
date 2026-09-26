package org.example.notification.delivery.application;

import java.util.UUID;
import org.example.notification.tenant.domain.ChannelType;

public class DeliveryCommand {
  private final UUID deliveryId, tenantId;
  private final int attemptNumber;
  private final ChannelType channel;
  private final String recipient, subject, body, providerIdempotencyKey;

  public DeliveryCommand(
      UUID deliveryId,
      UUID tenantId,
      int attemptNumber,
      ChannelType channel,
      String recipient,
      String subject,
      String body,
      String key) {
    this.deliveryId = deliveryId;
    this.tenantId = tenantId;
    this.attemptNumber = attemptNumber;
    this.channel = channel;
    this.recipient = recipient;
    this.subject = subject;
    this.body = body;
    this.providerIdempotencyKey = key;
  }

  public UUID getDeliveryId() {
    return deliveryId;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public int getAttemptNumber() {
    return attemptNumber;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public String getRecipient() {
    return recipient;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }

  public String getProviderIdempotencyKey() {
    return providerIdempotencyKey;
  }
}
