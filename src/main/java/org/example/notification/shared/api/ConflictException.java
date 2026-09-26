package org.example.notification.shared.api;

public class ConflictException extends RuntimeException {
  private final ApiErrorCode code;

  public ConflictException(ApiErrorCode code, String message) {
    super(message);
    this.code = code;
  }

  public ApiErrorCode getCode() {
    return code;
  }
}
