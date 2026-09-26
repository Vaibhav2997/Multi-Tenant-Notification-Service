package org.example.notification.delivery.domain;

public enum DeliveryStatus {
  SCHEDULED,
  QUEUED,
  PROCESSING,
  RETRY_SCHEDULED,
  DELIVERED,
  FAILED
}
