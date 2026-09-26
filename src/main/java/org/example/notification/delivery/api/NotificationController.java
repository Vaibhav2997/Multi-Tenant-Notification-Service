package org.example.notification.delivery.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.example.notification.auth.domain.CurrentUser;
import org.example.notification.delivery.api.dto.*;
import org.example.notification.delivery.application.*;
import org.example.notification.delivery.domain.DeliveryStatus;
import org.example.notification.shared.api.PageResponse;
import org.example.notification.tenant.application.TenantAccessService;
import org.example.notification.tenant.domain.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenant")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications and delivery reports")
public class NotificationController {
  private final TenantAccessService access;
  private final NotificationSubmissionService submissions;
  private final DeliveryQueryService queries;

  public NotificationController(
      TenantAccessService access,
      NotificationSubmissionService submissions,
      DeliveryQueryService queries) {
    this.access = access;
    this.submissions = submissions;
    this.queries = queries;
  }

  @PostMapping("/notification-batches")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Submit an immediate or scheduled notification batch")
  public NotificationBatchResponse submit(
      @AuthenticationPrincipal CurrentUser user,
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody CreateNotificationBatchRequest request) {
    Tenant tenant = access.requireTenant(user);
    return submissions.submit(tenant, user, idempotencyKey, request);
  }

  @GetMapping("/notification-batches/{id}")
  @Operation(summary = "Get notification batch status")
  public NotificationBatchResponse batch(
      @AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
    return submissions.get(access.requireTenant(user).getId(), id);
  }

  @GetMapping("/deliveries")
  @Operation(summary = "Search tenant delivery reports")
  public PageResponse<DeliverySummaryResponse> deliveries(
      @AuthenticationPrincipal CurrentUser user,
      @RequestParam(required = false) DeliveryStatus status,
      @RequestParam(required = false) ChannelType channel,
      @RequestParam(required = false) UUID batchId,
      @RequestParam(required = false) Instant createdFrom,
      @RequestParam(required = false) Instant createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size) {
    UUID tenantId = access.requireTenant(user).getId();
    Pageable pageable =
        PageRequest.of(
            Math.max(0, page),
            Math.min(100, Math.max(1, size)),
            Sort.by(Sort.Direction.DESC, "createdAt"));
    return PageResponse.from(
        queries.search(tenantId, status, channel, batchId, createdFrom, createdTo, pageable));
  }

  @GetMapping("/deliveries/{id}")
  @Operation(summary = "Get delivery content, attempts and audit timeline")
  public DeliveryDetailResponse delivery(
      @AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
    return queries.get(access.requireTenant(user).getId(), id);
  }
}
