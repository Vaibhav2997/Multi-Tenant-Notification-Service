package org.example.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import org.example.notification.tenant.application.*;
import org.example.notification.tenant.domain.PlatformPolicy;
import org.example.notification.tenant.infrastructure.TenantRateLimitRepository;
import org.junit.jupiter.api.Test;

class TenantRateLimiterTest {
  @Test
  void enforcesTheGlobalRateWhenTenantHasNoOverride() {
    TenantRateLimitRepository overrides = mock(TenantRateLimitRepository.class);
    PlatformAdministrationService platform = mock(PlatformAdministrationService.class);
    Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    when(overrides.findByTenantId(any())).thenReturn(Optional.empty());
    when(platform.requirePolicy()).thenReturn(new PlatformPolicy(clock.instant()));
    TenantRateLimiter limiter = new TenantRateLimiter(overrides, platform, clock);
    UUID tenantId = UUID.randomUUID();

    for (int i = 0; i < 60; i++) assertTrue(limiter.tryAcquire(tenantId));
    assertFalse(limiter.tryAcquire(tenantId));
  }
}
