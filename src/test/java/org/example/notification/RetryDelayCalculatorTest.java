package org.example.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.notification.delivery.application.RetryDelayCalculator;
import org.junit.jupiter.api.Test;

class RetryDelayCalculatorTest {
  private final RetryDelayCalculator calculator = new RetryDelayCalculator();

  @Test
  void appliesExponentialBackoffAndMaximumDelay() {
    assertEquals(5, calculator.calculateSeconds(1, 5, 12));
    assertEquals(10, calculator.calculateSeconds(2, 5, 12));
    assertEquals(12, calculator.calculateSeconds(3, 5, 12));
  }
}
