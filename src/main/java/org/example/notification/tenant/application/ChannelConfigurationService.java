package org.example.notification.tenant.application;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.*;
import org.example.notification.shared.api.ApiErrorCode;
import org.example.notification.shared.api.ConflictException;
import org.example.notification.shared.api.NotFoundException;
import org.example.notification.tenant.api.dto.*;
import org.example.notification.tenant.domain.*;
import org.example.notification.tenant.infrastructure.ChannelConfigurationRepository;
import org.springframework.stereotype.Service;

@Service
public class ChannelConfigurationService {
  private final ChannelConfigurationRepository channels;
  private final Clock clock;

  public ChannelConfigurationService(ChannelConfigurationRepository channels, Clock clock) {
    this.channels = channels;
    this.clock = clock;
  }

  public List<ChannelConfigurationResponse> list(UUID tenantId) {
    return channels.findByTenantIdOrderByChannel(tenantId).stream().map(this::response).toList();
  }

  @Transactional
  public ChannelConfigurationResponse update(
      UUID tenantId, ChannelType channel, ChannelConfigurationRequest request) {
    ChannelConfiguration configuration =
        channels
            .findByTenantIdAndChannel(tenantId, channel)
            .orElseThrow(() -> new NotFoundException("Channel configuration not found"));
    configuration.update(request.isEnabled(), request.getSenderIdentity(), clock.instant());
    return response(configuration);
  }

  public ChannelConfiguration requireEnabled(UUID tenantId, ChannelType channel) {
    ChannelConfiguration configuration =
        channels
            .findByTenantIdAndChannel(tenantId, channel)
            .orElseThrow(() -> new NotFoundException("Channel configuration not found"));
    if (!configuration.isEnabled())
      throw new ConflictException(
          ApiErrorCode.CHANNEL_DISABLED, "Channel " + channel + " is disabled");
    return configuration;
  }

  private ChannelConfigurationResponse response(ChannelConfiguration c) {
    return new ChannelConfigurationResponse(c.getChannel(), c.isEnabled(), c.getSenderIdentity());
  }
}
