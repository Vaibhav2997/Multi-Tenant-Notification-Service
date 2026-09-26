package org.example.notification.template.application;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.UUID;
import org.example.notification.shared.api.*;
import org.example.notification.template.api.dto.*;
import org.example.notification.template.domain.NotificationTemplate;
import org.example.notification.template.infrastructure.TemplateRepository;
import org.example.notification.tenant.domain.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
  private final TemplateRepository templates;
  private final Clock clock;

  public TemplateService(TemplateRepository templates, Clock clock) {
    this.templates = templates;
    this.clock = clock;
  }

  @Transactional
  public TemplateResponse create(Tenant tenant, CreateTemplateRequest request) {
    if (templates.existsByTenantIdAndNameAndChannel(
        tenant.getId(), request.getName(), request.getChannel()))
      throw new ConflictException(
          ApiErrorCode.CONFLICT, "Template name already exists for channel");
    validateSubject(request.getChannel(), request.getSubjectTemplate());
    NotificationTemplate template =
        templates.save(
            new NotificationTemplate(
                tenant,
                request.getName(),
                request.getChannel(),
                request.getSubjectTemplate(),
                request.getBodyTemplate(),
                clock.instant()));
    return response(template);
  }

  @Transactional
  public TemplateResponse update(UUID tenantId, UUID id, UpdateTemplateRequest request) {
    NotificationTemplate template = require(tenantId, id);
    validateSubject(template.getChannel(), request.getSubjectTemplate());
    template.update(
        request.getName(),
        request.getSubjectTemplate(),
        request.getBodyTemplate(),
        request.isActive(),
        clock.instant());
    return response(template);
  }

  public TemplateResponse get(UUID tenantId, UUID id) {
    return response(require(tenantId, id));
  }

  public Page<TemplateResponse> list(UUID tenantId, Pageable pageable) {
    return templates.findByTenantId(tenantId, pageable).map(this::response);
  }

  public NotificationTemplate requireActive(UUID tenantId, UUID id) {
    NotificationTemplate template = require(tenantId, id);
    if (!template.isActive())
      throw new ConflictException(ApiErrorCode.INVALID_STATE, "Template is inactive");
    return template;
  }

  private NotificationTemplate require(UUID tenantId, UUID id) {
    return templates
        .findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new NotFoundException("Template not found"));
  }

  private void validateSubject(ChannelType channel, String subject) {
    if (channel == ChannelType.EMAIL && (subject == null || subject.isBlank()))
      throw new IllegalArgumentException("Email templates require a subject");
  }

  private TemplateResponse response(NotificationTemplate t) {
    return new TemplateResponse(
        t.getId(),
        t.getName(),
        t.getChannel(),
        t.getSubjectTemplate(),
        t.getBodyTemplate(),
        t.isActive(),
        t.getCreatedAt(),
        t.getUpdatedAt());
  }
}
