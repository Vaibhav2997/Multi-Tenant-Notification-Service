package org.example.notification.template.infrastructure;

import java.util.*;
import org.example.notification.template.domain.NotificationTemplate;
import org.example.notification.tenant.domain.ChannelType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<NotificationTemplate, UUID> {
  Optional<NotificationTemplate> findByIdAndTenantId(UUID id, UUID tenantId);

  Page<NotificationTemplate> findByTenantId(UUID tenantId, Pageable pageable);

  boolean existsByTenantIdAndNameAndChannel(UUID tenantId, String name, ChannelType channel);
}
