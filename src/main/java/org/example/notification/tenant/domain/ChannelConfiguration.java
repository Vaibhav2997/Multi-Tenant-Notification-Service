package org.example.notification.tenant.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "channel_configurations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "channel"}))
public class ChannelConfiguration {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Tenant tenant;

  @Enumerated(EnumType.STRING)
  private ChannelType channel;

  private boolean enabled;
  private String senderIdentity;
  private Instant updatedAt;

  protected ChannelConfiguration() {}

  public ChannelConfiguration(Tenant tenant, ChannelType channel, Instant now) {
    this.tenant = tenant;
    this.channel = channel;
    this.updatedAt = now;
  }

  public void update(boolean enabled, String senderIdentity, Instant now) {
    this.enabled = enabled;
    this.senderIdentity = senderIdentity;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getSenderIdentity() {
    return senderIdentity;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
