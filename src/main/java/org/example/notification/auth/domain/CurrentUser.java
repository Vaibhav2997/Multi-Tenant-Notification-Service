package org.example.notification.auth.domain;

import java.security.Principal;
import java.util.UUID;

public final class CurrentUser implements Principal {
  private final UUID userId;
  private final UserRole role;
  private final UUID tenantId;

  public CurrentUser(UUID userId, UserRole role, UUID tenantId) {
    this.userId = userId;
    this.role = role;
    this.tenantId = tenantId;
  }

  @Override
  public String getName() {
    return userId.toString();
  }

  public UUID getUserId() {
    return userId;
  }

  public UserRole getRole() {
    return role;
  }

  public UUID getTenantId() {
    return tenantId;
  }
}
