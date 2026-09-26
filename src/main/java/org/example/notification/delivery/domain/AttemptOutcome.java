package org.example.notification.delivery.domain;

public enum AttemptOutcome {
  STARTED,
  DELIVERED,
  TRANSIENT_FAILURE,
  PERMANENT_FAILURE
}
