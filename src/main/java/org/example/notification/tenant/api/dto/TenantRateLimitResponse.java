package org.example.notification.tenant.api.dto;

import java.util.UUID;

public class TenantRateLimitResponse {
  private final UUID tenantId;
  private final int requestsPerMinute;
  private final boolean overridden;

  public TenantRateLimitResponse(UUID tenantId, int rate, boolean overridden) {
    this.tenantId = tenantId;
    this.requestsPerMinute = rate;
    this.overridden = overridden;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public int getRequestsPerMinute() {
    return requestsPerMinute;
  }

  public boolean isOverridden() {
    return overridden;
  }
}
