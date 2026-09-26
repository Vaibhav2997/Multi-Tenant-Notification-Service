package org.example.notification.tenant.api.dto;

import jakarta.validation.constraints.Size;

public class ChannelConfigurationRequest {
  private boolean enabled;

  @Size(max = 180)
  private String senderIdentity;

  public ChannelConfigurationRequest() {}

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getSenderIdentity() {
    return senderIdentity;
  }

  public void setSenderIdentity(String senderIdentity) {
    this.senderIdentity = senderIdentity;
  }
}
