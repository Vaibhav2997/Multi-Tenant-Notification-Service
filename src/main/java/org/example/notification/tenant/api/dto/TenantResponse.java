package org.example.notification.tenant.api.dto;

import java.time.Instant;
import java.util.UUID;

public class TenantResponse {
  private final UUID id;
  private final String name;
  private final String slug;
  private final Instant createdAt;

  public TenantResponse(UUID id, String name, String slug, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSlug() {
    return slug;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
