package org.example.notification.delivery.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.example.notification.tenant.domain.ChannelType;

@Entity
@Table(name = "provider_receipts")
public class ProviderReceipt {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String providerIdempotencyKey;

  @Enumerated(EnumType.STRING)
  private ChannelType channel;

  private String providerReference;
  private Instant acceptedAt;

  protected ProviderReceipt() {}

  public ProviderReceipt(String key, ChannelType channel, String reference, Instant now) {
    providerIdempotencyKey = key;
    this.channel = channel;
    providerReference = reference;
    acceptedAt = now;
  }

  public String getProviderReference() {
    return providerReference;
  }
}
