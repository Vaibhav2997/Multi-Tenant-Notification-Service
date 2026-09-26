package org.example.notification.delivery.infrastructure;

import org.example.notification.delivery.application.*;

public interface ProviderOutcomeResolver {
  ProviderResult resolve(DeliveryCommand command);
}
