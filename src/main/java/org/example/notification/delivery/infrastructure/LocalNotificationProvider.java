package org.example.notification.delivery.infrastructure;

import jakarta.transaction.Transactional;
import java.time.Clock;
import org.example.notification.delivery.application.*;
import org.example.notification.delivery.domain.*;
import org.springframework.stereotype.Component;

@Component
public class LocalNotificationProvider implements NotificationProvider {
  private final ProviderReceiptRepository receipts;
  private final ProviderOutcomeResolver resolver;
  private final Clock clock;

  public LocalNotificationProvider(
      ProviderReceiptRepository receipts, ProviderOutcomeResolver resolver, Clock clock) {
    this.receipts = receipts;
    this.resolver = resolver;
    this.clock = clock;
  }

  @Override
  @Transactional
  public ProviderResult send(DeliveryCommand command) {
    return receipts
        .findByProviderIdempotencyKey(command.getProviderIdempotencyKey())
        .map(receipt -> ProviderResult.success(receipt.getProviderReference()))
        .orElseGet(
            () -> {
              ProviderResult result = resolver.resolve(command);
              if (result.getOutcome() == ProviderOutcome.SUCCESS)
                receipts.save(
                    new ProviderReceipt(
                        command.getProviderIdempotencyKey(),
                        command.getChannel(),
                        result.getProviderReference(),
                        clock.instant()));
              return result;
            });
  }
}
