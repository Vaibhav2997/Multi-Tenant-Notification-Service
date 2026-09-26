package org.example.notification.template.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.example.notification.tenant.domain.*;

@Entity
@Table(
    name = "templates",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name", "channel"}))
public class NotificationTemplate {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Tenant tenant;

  private String name;

  @Enumerated(EnumType.STRING)
  private ChannelType channel;

  private String subjectTemplate;

  @Column(columnDefinition = "text")
  private String bodyTemplate;

  private boolean active;
  private Instant createdAt;
  private Instant updatedAt;

  protected NotificationTemplate() {}

  public NotificationTemplate(
      Tenant tenant, String name, ChannelType channel, String subject, String body, Instant now) {
    this.tenant = tenant;
    this.name = name;
    this.channel = channel;
    this.subjectTemplate = subject;
    this.bodyTemplate = body;
    this.active = true;
    this.createdAt = now;
    this.updatedAt = now;
  }

  public void update(String name, String subject, String body, boolean active, Instant now) {
    this.name = name;
    this.subjectTemplate = subject;
    this.bodyTemplate = body;
    this.active = active;
    this.updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public String getName() {
    return name;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public String getSubjectTemplate() {
    return subjectTemplate;
  }

  public String getBodyTemplate() {
    return bodyTemplate;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
