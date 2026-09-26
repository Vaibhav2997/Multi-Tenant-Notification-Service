package org.example.notification.auth.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.notification.auth.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, UUID> {
  Optional<AppUser> findByEmailIgnoreCase(String email);

  List<AppUser> findByTenantId(UUID tenantId);
}
