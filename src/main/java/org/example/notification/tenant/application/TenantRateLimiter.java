package org.example.notification.tenant.application;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.example.notification.tenant.infrastructure.TenantRateLimitRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantRateLimiter {
  private final TenantRateLimitRepository overrides;
  private final PlatformAdministrationService platform;
  private final Clock clock;
  private final Map<UUID, Bucket> buckets = new ConcurrentHashMap<>();

  public TenantRateLimiter(
      TenantRateLimitRepository overrides, PlatformAdministrationService platform, Clock clock) {
    this.overrides = overrides;
    this.platform = platform;
    this.clock = clock;
  }

  public boolean tryAcquire(UUID tenantId) {
    int rate =
        overrides
            .findByTenantId(tenantId)
            .map(v -> v.getRequestsPerMinute())
            .orElseGet(() -> platform.requirePolicy().getDefaultRatePerMinute());
    return buckets
        .computeIfAbsent(tenantId, id -> new Bucket(rate, clock.instant()))
        .tryAcquire(rate, clock.instant());
  }

  private static final class Bucket {
    private double tokens;
    private Instant lastRefill;

    private Bucket(int capacity, Instant now) {
      tokens = capacity;
      lastRefill = now;
    }

    private synchronized boolean tryAcquire(int capacity, Instant now) {
      tokens =
          Math.min(
              capacity, tokens + Duration.between(lastRefill, now).toMillis() * capacity / 60000d);
      lastRefill = now;
      if (tokens < 1) return false;
      tokens--;
      return true;
    }
  }
}
