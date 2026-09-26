package org.example.notification.template.api.dto;

import jakarta.validation.constraints.*;
import org.example.notification.tenant.domain.ChannelType;

public class CreateTemplateRequest {
  @NotBlank
  @Size(max = 120)
  private String name;

  @NotNull private ChannelType channel;

  @Size(max = 500)
  private String subjectTemplate;

  @NotBlank private String bodyTemplate;

  public CreateTemplateRequest() {}

  public String getName() {
    return name;
  }

  public void setName(String v) {
    name = v;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public void setChannel(ChannelType v) {
    channel = v;
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
}
