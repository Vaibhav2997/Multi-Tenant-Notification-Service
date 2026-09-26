package org.example.notification.tenant.api.dto;

import jakarta.validation.constraints.*;

public class CreateTenantRequest {
  @NotBlank
  @Size(max = 120)
  private String name;

  @NotBlank
  @Pattern(regexp = "[a-z0-9-]+")
  private String slug;

  public CreateTenantRequest() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }
}
