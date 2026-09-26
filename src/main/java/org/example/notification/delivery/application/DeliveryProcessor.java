package org.example.notification.delivery.application;

import java.util.UUID;
import org.example.notification.tenant.application.TenantRateLimiter;
import org.springframework.stereotype.Service;

@Service
public class DeliveryProcessor {
  private final DeliveryStateService states;
  private final TenantRateLimiter limiter;
  private final NotificationProvider provider;

  public DeliveryProcessor(
      DeliveryStateService states, TenantRateLimiter limiter, NotificationProvider provider) {
    this.states = states;
    this.limiter = limiter;
    this.provider = provider;
  }

  public void process(UUID deliveryId, UUID tenantId) {
    if (!limiter.tryAcquire(tenantId)) {
      states.deferRateLimited(deliveryId);
      return;
    }
    DeliveryCommand command = states.claim(deliveryId);
    if (command == null) return;
    ProviderResult result;
    try {
      result = provider.send(command);
    } catch (Exception exception) {
      result = ProviderResult.transientFailure("PROVIDER_EXCEPTION", exception.getMessage());
    }
    states.complete(deliveryId, result);
  }
}
