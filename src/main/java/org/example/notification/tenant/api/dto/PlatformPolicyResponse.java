package org.example.notification.tenant.api.dto;

public class PlatformPolicyResponse {
  private final int defaultRatePerMinute,
      maxBatchSize,
      maxAttempts,
      baseRetryDelaySeconds,
      maxRetryDelaySeconds;

  public PlatformPolicyResponse(int rate, int batch, int attempts, int base, int max) {
    defaultRatePerMinute = rate;
    maxBatchSize = batch;
    maxAttempts = attempts;
    baseRetryDelaySeconds = base;
    maxRetryDelaySeconds = max;
  }

  public int getDefaultRatePerMinute() {
    return defaultRatePerMinute;
  }

  public int getMaxBatchSize() {
    return maxBatchSize;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public int getBaseRetryDelaySeconds() {
    return baseRetryDelaySeconds;
  }

  public int getMaxRetryDelaySeconds() {
    return maxRetryDelaySeconds;
  }
}
