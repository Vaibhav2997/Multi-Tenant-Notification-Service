package org.example.notification.delivery.application;

import org.springframework.stereotype.Component;

@Component
public class RetryDelayCalculator {
  public long calculateSeconds(int attemptNumber, int baseDelaySeconds, int maxDelaySeconds) {
    if (attemptNumber < 1 || baseDelaySeconds < 1 || maxDelaySeconds < baseDelaySeconds) {
      throw new IllegalArgumentException("Invalid retry policy");
    }
    long multiplier = 1L << Math.min(20, attemptNumber - 1);
    return Math.min(maxDelaySeconds, baseDelaySeconds * multiplier);
  }
}
