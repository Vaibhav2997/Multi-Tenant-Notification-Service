package org.example.notification.delivery.api.dto;

import java.util.List;

public class DeliveryDetailResponse {
  private final DeliverySummaryResponse delivery;
  private final String renderedSubject, renderedBody;
  private final List<DeliveryAttemptResponse> attempts;
  private final List<DeliveryEventResponse> events;

  public DeliveryDetailResponse(
      DeliverySummaryResponse delivery,
      String subject,
      String body,
      List<DeliveryAttemptResponse> attempts,
      List<DeliveryEventResponse> events) {
    this.delivery = delivery;
    renderedSubject = subject;
    renderedBody = body;
    this.attempts = attempts;
    this.events = events;
  }

  public DeliverySummaryResponse getDelivery() {
    return delivery;
  }

  public String getRenderedSubject() {
    return renderedSubject;
  }

  public String getRenderedBody() {
    return renderedBody;
  }

  public List<DeliveryAttemptResponse> getAttempts() {
    return attempts;
  }

  public List<DeliveryEventResponse> getEvents() {
    return events;
  }
}
