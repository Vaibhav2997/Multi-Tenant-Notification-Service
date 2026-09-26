package org.example.notification.delivery.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public class CreateNotificationBatchRequest {
  @NotNull private UUID templateId;
  private Instant scheduledAt;
  private Map<String, String> variables;
  @NotEmpty private List<@Valid RecipientRequest> recipients;

  public CreateNotificationBatchRequest() {}

  public UUID getTemplateId() {
    return templateId;
  }

  public void setTemplateId(UUID v) {
    templateId = v;
  }

  public Instant getScheduledAt() {
    return scheduledAt;
  }

  public void setScheduledAt(Instant v) {
    scheduledAt = v;
  }

  public Map<String, String> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, String> v) {
    variables = v;
  }

  public List<RecipientRequest> getRecipients() {
    return recipients;
  }

  public void setRecipients(List<RecipientRequest> v) {
    recipients = v;
  }
}
