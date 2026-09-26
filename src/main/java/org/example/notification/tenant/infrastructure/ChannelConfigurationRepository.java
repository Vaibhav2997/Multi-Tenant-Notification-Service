package org.example.notification.tenant.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.notification.tenant.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelConfigurationRepository extends JpaRepository<ChannelConfiguration, UUID> {
  Optional<ChannelConfiguration> findByTenantIdAndChannel(UUID tenantId, ChannelType channel);

  List<ChannelConfiguration> findByTenantIdOrderByChannel(UUID tenantId);
}
