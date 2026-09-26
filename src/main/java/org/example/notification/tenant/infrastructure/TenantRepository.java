package org.example.notification.tenant.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.example.notification.tenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
  Optional<Tenant> findBySlug(String slug);
}
