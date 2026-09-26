package org.example.notification.tenant.api.dto;

import java.util.UUID;

public class TenantAdminResponse {
  private final UUID id;
  private final String email;
  private final UUID tenantId;

  public TenantAdminResponse(UUID id, String email, UUID tenantId) {
    this.id = id;
    this.email = email;
    this.tenantId = tenantId;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public UUID getTenantId() {
    return tenantId;
  }
}
