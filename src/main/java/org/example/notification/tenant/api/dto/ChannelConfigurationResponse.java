package org.example.notification.tenant.api.dto;

import org.example.notification.tenant.domain.ChannelType;

public class ChannelConfigurationResponse {
  private final ChannelType channel;
  private final boolean enabled;
  private final String senderIdentity;

  public ChannelConfigurationResponse(ChannelType channel, boolean enabled, String senderIdentity) {
    this.channel = channel;
    this.enabled = enabled;
    this.senderIdentity = senderIdentity;
  }

  public ChannelType getChannel() {
    return channel;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getSenderIdentity() {
    return senderIdentity;
  }
}
