package org.example.notification.tenant.api.dto;

import jakarta.validation.constraints.*;

public class PlatformPolicyRequest {
  @Min(1)
  @Max(100000)
  private int defaultRatePerMinute;

  @Min(1)
  @Max(1000)
  private int maxBatchSize;

  @Min(1)
  @Max(20)
  private int maxAttempts;

  @Min(1)
  @Max(3600)
  private int baseRetryDelaySeconds;

  @Min(1)
  @Max(86400)
  private int maxRetryDelaySeconds;

  public PlatformPolicyRequest() {}

  public int getDefaultRatePerMinute() {
    return defaultRatePerMinute;
  }

  public void setDefaultRatePerMinute(int v) {
    defaultRatePerMinute = v;
  }

  public int getMaxBatchSize() {
    return maxBatchSize;
  }

  public void setMaxBatchSize(int v) {
    maxBatchSize = v;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public void setMaxAttempts(int v) {
    maxAttempts = v;
  }

  public int getBaseRetryDelaySeconds() {
    return baseRetryDelaySeconds;
  }

  public void setBaseRetryDelaySeconds(int v) {
    baseRetryDelaySeconds = v;
  }

  public int getMaxRetryDelaySeconds() {
    return maxRetryDelaySeconds;
  }

  public void setMaxRetryDelaySeconds(int v) {
    maxRetryDelaySeconds = v;
  }
}
