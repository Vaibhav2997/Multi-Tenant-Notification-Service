package org.example.notification.tenant.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_rate_limits")
public class TenantRateLimit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  private Tenant tenant;

  private int requestsPerMinute;
  private Instant updatedAt;

  protected TenantRateLimit() {}

  public TenantRateLimit(Tenant tenant, int requestsPerMinute, Instant now) {
    this.tenant = tenant;
    this.requestsPerMinute = requestsPerMinute;
    this.updatedAt = now;
  }

  public void update(int requestsPerMinute, Instant now) {
    this.requestsPerMinute = requestsPerMinute;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public int getRequestsPerMinute() {
    return requestsPerMinute;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
