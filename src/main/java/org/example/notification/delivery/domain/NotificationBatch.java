package org.example.notification.delivery.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.example.notification.auth.domain.AppUser;
import org.example.notification.template.domain.NotificationTemplate;
import org.example.notification.tenant.domain.Tenant;

@Entity
@Table(
    name = "notification_batches",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "idempotency_key"}))
public class NotificationBatch {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Tenant tenant;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private NotificationTemplate template;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "requested_by")
  private AppUser requestedBy;

  private String idempotencyKey;
  private String requestHash;
  private Instant scheduledAt;

  @Enumerated(EnumType.STRING)
  private BatchStatus status;

  private Instant createdAt;
  private Instant updatedAt;

  protected NotificationBatch() {}

  public NotificationBatch(
      Tenant tenant,
      NotificationTemplate template,
      AppUser requestedBy,
      String key,
      String hash,
      Instant scheduledAt,
      Instant now) {
    this.tenant = tenant;
    this.template = template;
    this.requestedBy = requestedBy;
    this.idempotencyKey = key;
    this.requestHash = hash;
    this.scheduledAt = scheduledAt;
    this.status = BatchStatus.ACCEPTED;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void changeStatus(BatchStatus status, Instant now) {
    this.status = status;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public NotificationTemplate getTemplate() {
    return template;
  }

  public AppUser getRequestedBy() {
    return requestedBy;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getRequestHash() {
    return requestHash;
  }

  public Instant getScheduledAt() {
    return scheduledAt;
  }

  public BatchStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
