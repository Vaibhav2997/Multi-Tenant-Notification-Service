package org.example.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.example.notification.delivery.application.DeliveryCommand;
import org.example.notification.delivery.domain.ProviderOutcome;
import org.example.notification.delivery.infrastructure.LocalProviderOutcomeResolver;
import org.example.notification.tenant.domain.ChannelType;
import org.junit.jupiter.api.Test;

class LocalProviderOutcomeResolverTest {
  private final LocalProviderOutcomeResolver resolver = new LocalProviderOutcomeResolver();

  @Test
  void transientOnceFailsFirstAttemptThenSucceeds() {
    assertEquals(
        ProviderOutcome.TRANSIENT_FAILURE,
        resolver.resolve(command("transient-once@provider.test", 1)).getOutcome());
    assertEquals(
        ProviderOutcome.SUCCESS,
        resolver.resolve(command("transient-once@provider.test", 2)).getOutcome());
  }

  @Test
  void supportsPermanentAndExhaustibleTransientFailures() {
    assertEquals(
        ProviderOutcome.PERMANENT_FAILURE,
        resolver.resolve(command("permanent-failure@provider.test", 1)).getOutcome());
    assertEquals(
        ProviderOutcome.TRANSIENT_FAILURE,
        resolver.resolve(command("transient-always@provider.test", 3)).getOutcome());
  }

  @Test
  void normalRecipientSucceeds() {
    assertEquals(
        ProviderOutcome.SUCCESS,
        resolver.resolve(command("customer@example.test", 1)).getOutcome());
  }

  private DeliveryCommand command(String recipient, int attemptNumber) {
    return new DeliveryCommand(
        UUID.randomUUID(),
        UUID.randomUUID(),
        attemptNumber,
        ChannelType.EMAIL,
        recipient,
        "subject",
        "body",
        UUID.randomUUID().toString());
  }
}
