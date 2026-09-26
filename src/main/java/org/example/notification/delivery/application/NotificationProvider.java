package org.example.notification.delivery.application;

public interface NotificationProvider {
  ProviderResult send(DeliveryCommand command);
}
