package org.example.notification.template.api.dto;

import java.time.Instant;
import java.util.UUID;
import org.example.notification.tenant.domain.ChannelType;

public class TemplateResponse {
  private final UUID id;
  private final String name;
  private final ChannelType channel;
  private final String subjectTemplate;
  private final String bodyTemplate;
  private final boolean active;
  private final Instant createdAt;
  private final Instant updatedAt;

  public TemplateResponse(
      UUID id,
      String name,
      ChannelType channel,
      String subject,
      String body,
      boolean active,
      Instant created,
      Instant updated) {
    this.id = id;
    this.name = name;
    this.channel = channel;
    this.subjectTemplate = subject;
    this.bodyTemplate = body;
    this.active = active;
    this.createdAt = created;
    this.updatedAt = updated;
  }

  public UUID getId() {
    return id;
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
