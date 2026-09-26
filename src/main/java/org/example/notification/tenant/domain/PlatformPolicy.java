package org.example.notification.tenant.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "platform_policy")
public class PlatformPolicy {
  public static final long SINGLETON_ID = 1L;

  @Id private Long id;
  private int defaultRatePerMinute;
  private int maxBatchSize;
  private int maxAttempts;
  private int baseRetryDelaySeconds;
  private int maxRetryDelaySeconds;
  private Instant updatedAt;

  protected PlatformPolicy() {}

  public PlatformPolicy(Instant now) {
    this.id = SINGLETON_ID;
    this.defaultRatePerMinute = 60;
    this.maxBatchSize = 100;
    this.maxAttempts = 3;
    this.baseRetryDelaySeconds = 5;
    this.maxRetryDelaySeconds = 300;
    this.updatedAt = now;
  }

  public void update(
      int rate, int batchSize, int attempts, int baseDelay, int maxDelay, Instant now) {
    this.defaultRatePerMinute = rate;
    this.maxBatchSize = batchSize;
    this.maxAttempts = attempts;
    this.baseRetryDelaySeconds = baseDelay;
    this.maxRetryDelaySeconds = maxDelay;
    this.updatedAt = now;
  }

  public Long getId() {
    return id;
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

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
