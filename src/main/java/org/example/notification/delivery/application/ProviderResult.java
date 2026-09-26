package org.example.notification.delivery.application;

import org.example.notification.delivery.domain.ProviderOutcome;

public class ProviderResult {
  private final ProviderOutcome outcome;
  private final String providerReference, errorCode, message;

  public ProviderResult(ProviderOutcome outcome, String reference, String code, String message) {
    this.outcome = outcome;
    providerReference = reference;
    errorCode = code;
    this.message = message;
  }

  public static ProviderResult success(String reference) {
    return new ProviderResult(ProviderOutcome.SUCCESS, reference, null, null);
  }

  public static ProviderResult transientFailure(String code, String message) {
    return new ProviderResult(ProviderOutcome.TRANSIENT_FAILURE, null, code, message);
  }

  public static ProviderResult permanentFailure(String code, String message) {
    return new ProviderResult(ProviderOutcome.PERMANENT_FAILURE, null, code, message);
  }

  public ProviderOutcome getOutcome() {
    return outcome;
  }

  public String getProviderReference() {
    return providerReference;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getMessage() {
    return message;
  }
}
