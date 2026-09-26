package org.example.notification.tenant.api.dto;

import jakarta.validation.constraints.*;

public class CreateTenantAdminRequest {
  @NotBlank @Email private String email;

  @NotBlank
  @Size(min = 8, max = 100)
  private String password;

  public CreateTenantAdminRequest() {}

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
