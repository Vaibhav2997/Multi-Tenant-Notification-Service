package org.example.notification.delivery.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "delivery_attempts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"delivery_id", "attempt_number"}))
public class DeliveryAttempt {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Delivery delivery;

  private int attemptNumber;

  @Enumerated(EnumType.STRING)
  private AttemptOutcome outcome;

  private String providerReference;
  private String errorCode;
  private String errorMessage;
  private Instant startedAt;
  private Instant completedAt;

  protected DeliveryAttempt() {}

  public DeliveryAttempt(Delivery delivery, int number, Instant now) {
    this.delivery = delivery;
    this.attemptNumber = number;
    this.outcome = AttemptOutcome.STARTED;
    this.startedAt = now;
  }

  public void complete(
      AttemptOutcome outcome, String reference, String code, String message, Instant now) {
    this.outcome = outcome;
    this.providerReference = reference;
    this.errorCode = code;
    this.errorMessage = message;
    this.completedAt = now;
  }

  public UUID getId() {
    return id;
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
