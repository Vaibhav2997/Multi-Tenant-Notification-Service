package org.example.notification.template.api.dto;

import jakarta.validation.constraints.*;

public class UpdateTemplateRequest {
  @NotBlank
  @Size(max = 120)
  private String name;

  @Size(max = 500)
  private String subjectTemplate;

  @NotBlank private String bodyTemplate;
  private boolean active;

  public UpdateTemplateRequest() {}

  public String getName() {
    return name;
  }

  public void setName(String v) {
    name = v;
  }

  public String getSubjectTemplate() {
    return subjectTemplate;
  }

  public void setSubjectTemplate(String v) {
    subjectTemplate = v;
  }

  public String getBodyTemplate() {
    return bodyTemplate;
  }

  public void setBodyTemplate(String v) {
    bodyTemplate = v;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean v) {
    active = v;
  }
}
