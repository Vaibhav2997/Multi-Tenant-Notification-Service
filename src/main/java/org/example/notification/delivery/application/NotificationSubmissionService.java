package org.example.notification.delivery.application;

import jakarta.transaction.Transactional;
import java.time.*;
import java.util.*;
import org.example.notification.auth.domain.*;
import org.example.notification.auth.infrastructure.UserRepository;
import org.example.notification.delivery.api.dto.*;
import org.example.notification.delivery.domain.*;
import org.example.notification.delivery.infrastructure.*;
import org.example.notification.shared.api.*;
import org.example.notification.template.application.*;
import org.example.notification.template.domain.NotificationTemplate;
import org.example.notification.tenant.application.*;
import org.example.notification.tenant.domain.Tenant;
import org.springframework.stereotype.Service;

@Service
public class NotificationSubmissionService {
  private final BatchRepository batches;
  private final DeliveryRepository deliveries;
  private final DeliveryEventRepository events;
  private final UserRepository users;
  private final TemplateService templates;
  private final TemplateRenderer renderer;
  private final ChannelConfigurationService channels;
  private final PlatformAdministrationService platform;
  private final RequestHashService hashes;
  private final Clock clock;

  public NotificationSubmissionService(
      BatchRepository batches,
      DeliveryRepository deliveries,
      DeliveryEventRepository events,
      UserRepository users,
      TemplateService templates,
      TemplateRenderer renderer,
      ChannelConfigurationService channels,
      PlatformAdministrationService platform,
      RequestHashService hashes,
      Clock clock) {
    this.batches = batches;
    this.deliveries = deliveries;
    this.events = events;
    this.users = users;
    this.templates = templates;
    this.renderer = renderer;
    this.channels = channels;
    this.platform = platform;
    this.hashes = hashes;
    this.clock = clock;
  }

  @Transactional
  public NotificationBatchResponse submit(
      Tenant tenant, CurrentUser principal, String key, CreateNotificationBatchRequest request) {
    if (key == null || key.isBlank())
      throw new IllegalArgumentException("Idempotency-Key header is required");
    if (request.getRecipients().size() > platform.requirePolicy().getMaxBatchSize())
      throw new IllegalArgumentException(
          "Batch exceeds maximum size of " + platform.requirePolicy().getMaxBatchSize());
    Instant now = clock.instant();
    if (request.getScheduledAt() != null && !request.getScheduledAt().isAfter(now))
      throw new IllegalArgumentException("scheduledAt must be in the future");
    String hash = hashes.hash(request);
    Optional<NotificationBatch> existing =
        batches.findByTenantIdAndIdempotencyKey(tenant.getId(), key);
    if (existing.isPresent()) {
      if (!existing.get().getRequestHash().equals(hash))
        throw new ConflictException(
            ApiErrorCode.IDEMPOTENCY_CONFLICT,
            "Idempotency key was already used with a different request");
      return response(existing.get());
    }
    NotificationTemplate template =
        templates.requireActive(tenant.getId(), request.getTemplateId());
    channels.requireEnabled(tenant.getId(), template.getChannel());
    AppUser user =
        users
            .findById(principal.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    NotificationBatch batch =
        batches.save(
            new NotificationBatch(
                tenant, template, user, key, hash, request.getScheduledAt(), now));
    for (RecipientRequest recipient : request.getRecipients()) {
      Map<String, String> variables = new HashMap<>();
      if (request.getVariables() != null) variables.putAll(request.getVariables());
      if (recipient.getVariables() != null) variables.putAll(recipient.getVariables());
      String subject = renderer.render(template.getSubjectTemplate(), variables);
      String body = renderer.render(template.getBodyTemplate(), variables);
      DeliveryStatus status =
          request.getScheduledAt() == null ? DeliveryStatus.QUEUED : DeliveryStatus.SCHEDULED;
      Instant due = request.getScheduledAt() == null ? now : request.getScheduledAt();
      Delivery delivery =
          deliveries.save(
              new Delivery(
                  tenant,
                  batch,
                  template.getChannel(),
                  recipient.getAddress(),
                  subject,
                  body,
                  status,
                  due,
                  UUID.randomUUID().toString(),
                  now));
      events.save(new DeliveryEvent(delivery, null, status, "Notification accepted", now));
    }
    return response(batch);
  }

  public NotificationBatchResponse get(UUID tenantId, UUID batchId) {
    NotificationBatch batch =
        batches
            .findByIdAndTenantId(batchId, tenantId)
            .orElseThrow(() -> new NotFoundException("Notification batch not found"));
    return response(batch);
  }

  private NotificationBatchResponse response(NotificationBatch batch) {
    List<Delivery> items = deliveries.findByBatchId(batch.getId());
    long delivered = items.stream().filter(d -> d.getStatus() == DeliveryStatus.DELIVERED).count();
    long failed = items.stream().filter(d -> d.getStatus() == DeliveryStatus.FAILED).count();
    return new NotificationBatchResponse(
        batch.getId(),
        batch.getStatus(),
        items.size(),
        delivered,
        failed,
        batch.getScheduledAt(),
        batch.getCreatedAt());
  }
}
