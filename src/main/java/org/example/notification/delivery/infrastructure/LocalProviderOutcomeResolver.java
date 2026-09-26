package org.example.notification.delivery.infrastructure;

import java.util.Locale;
import java.util.UUID;
import org.example.notification.delivery.application.DeliveryCommand;
import org.example.notification.delivery.application.ProviderResult;
import org.springframework.stereotype.Component;

/** Deterministic outcomes for the built-in local provider used by development and demos. */
@Component
public class LocalProviderOutcomeResolver implements ProviderOutcomeResolver {
  static final String TRANSIENT_ONCE_RECIPIENT = "transient-once@provider.test";
  static final String TRANSIENT_ALWAYS_RECIPIENT = "transient-always@provider.test";
  static final String PERMANENT_FAILURE_RECIPIENT = "permanent-failure@provider.test";

  @Override
  public ProviderResult resolve(DeliveryCommand command) {
    String recipient = command.getRecipient().toLowerCase(Locale.ROOT);

    if (PERMANENT_FAILURE_RECIPIENT.equals(recipient)) {
      return ProviderResult.permanentFailure(
          "RECIPIENT_REJECTED", "Local provider simulated a permanent rejection");
    }
    if (TRANSIENT_ALWAYS_RECIPIENT.equals(recipient)
        || (TRANSIENT_ONCE_RECIPIENT.equals(recipient) && command.getAttemptNumber() == 1)) {
      return ProviderResult.transientFailure(
          "PROVIDER_UNAVAILABLE", "Local provider simulated a transient outage");
    }
    return ProviderResult.success("local-" + UUID.randomUUID());
  }
}
