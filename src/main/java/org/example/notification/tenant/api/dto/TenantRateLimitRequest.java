package org.example.notification.tenant.api.dto;

import jakarta.validation.constraints.*;

public class TenantRateLimitRequest {
  @Min(1)
  @Max(100000)
  private int requestsPerMinute;

  public TenantRateLimitRequest() {}

  public int getRequestsPerMinute() {
    return requestsPerMinute;
  }

  public void setRequestsPerMinute(int value) {
    requestsPerMinute = value;
  }
}
