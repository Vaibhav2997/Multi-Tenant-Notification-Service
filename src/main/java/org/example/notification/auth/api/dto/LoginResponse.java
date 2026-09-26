package org.example.notification.auth.api.dto;

import java.util.UUID;

public class LoginResponse {
  private final String accessToken;
  private final String tokenType;
  private final String role;
  private final UUID tenantId;

  public LoginResponse(String accessToken, String role, UUID tenantId) {
    this.accessToken = accessToken;
    this.tokenType = "Bearer";
    this.role = role;
    this.tenantId = tenantId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getRole() {
    return role;
  }

  public UUID getTenantId() {
    return tenantId;
  }
}
