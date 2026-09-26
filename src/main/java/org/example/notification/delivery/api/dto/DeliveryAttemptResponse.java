package org.example.notification.delivery.api.dto;

import java.time.Instant;
import org.example.notification.delivery.domain.AttemptOutcome;

public class DeliveryAttemptResponse {
  private final int attemptNumber;
  private final AttemptOutcome outcome;
  private final String providerReference, errorCode, errorMessage;
  private final Instant startedAt, completedAt;

  public DeliveryAttemptResponse(
      int n,
      AttemptOutcome o,
      String ref,
      String code,
      String message,
      Instant started,
      Instant completed) {
    attemptNumber = n;
    outcome = o;
    providerReference = ref;
    errorCode = code;
    errorMessage = message;
    startedAt = started;
    completedAt = completed;
  }

  public int getAttemptNumber() {
    return attemptNumber;
  }

  public AttemptOutcome getOutcome() {
    return outcome;
  }

  public String getProviderReference() {
    return providerReference;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }
}
