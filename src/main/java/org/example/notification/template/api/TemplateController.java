package org.example.notification.template.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.example.notification.auth.domain.CurrentUser;
import org.example.notification.shared.api.PageResponse;
import org.example.notification.template.api.dto.*;
import org.example.notification.template.application.TemplateService;
import org.example.notification.tenant.application.TenantAccessService;
import org.example.notification.tenant.domain.Tenant;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenant/templates")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Templates")
public class TemplateController {
  private final TenantAccessService access;
  private final TemplateService templates;

  public TemplateController(TenantAccessService access, TemplateService templates) {
    this.access = access;
    this.templates = templates;
  }

  @PostMapping
  @Operation(summary = "Create a tenant template")
  public TemplateResponse create(
      @AuthenticationPrincipal CurrentUser user,
      @Valid @RequestBody CreateTemplateRequest request) {
    return templates.create(access.requireTenant(user), request);
  }

  @GetMapping
  @Operation(summary = "List tenant templates")
  public PageResponse<TemplateResponse> list(
      @AuthenticationPrincipal CurrentUser user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size) {
    Tenant tenant = access.requireTenant(user);
    return PageResponse.from(
        templates.list(
            tenant.getId(),
            PageRequest.of(
                Math.max(0, page),
                Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "createdAt"))));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a tenant template")
  public TemplateResponse get(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
    return templates.get(access.requireTenant(user).getId(), id);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update or activate a tenant template")
  public TemplateResponse update(
      @AuthenticationPrincipal CurrentUser user,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateTemplateRequest request) {
    return templates.update(access.requireTenant(user).getId(), id, request);
  }
}
