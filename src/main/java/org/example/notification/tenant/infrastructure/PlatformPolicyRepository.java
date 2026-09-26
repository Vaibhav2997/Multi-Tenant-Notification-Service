package org.example.notification.tenant.infrastructure;

import org.example.notification.tenant.domain.PlatformPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformPolicyRepository extends JpaRepository<PlatformPolicy, Long> {}
