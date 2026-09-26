package org.example.notification.tenant.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.example.notification.tenant.domain.TenantRateLimit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRateLimitRepository extends JpaRepository<TenantRateLimit, UUID> {
  Optional<TenantRateLimit> findByTenantId(UUID tenantId);
}
