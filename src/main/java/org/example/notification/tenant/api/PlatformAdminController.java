package org.example.notification.tenant.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import org.example.notification.tenant.api.dto.*;
import org.example.notification.tenant.application.PlatformAdministrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Platform administration")
public class PlatformAdminController {
  private final PlatformAdministrationService service;

  public PlatformAdminController(PlatformAdministrationService service) {
    this.service = service;
  }

  @PostMapping("/tenants")
  @Operation(summary = "Create a tenant")
  public TenantResponse createTenant(@Valid @RequestBody CreateTenantRequest request) {
    return service.createTenant(request);
  }

  @GetMapping("/tenants")
  @Operation(summary = "List tenants")
  public List<TenantResponse> listTenants() {
    return service.listTenants();
  }

  @PostMapping("/tenants/{tenantId}/admins")
  @Operation(summary = "Create a tenant administrator")
  public TenantAdminResponse createAdmin(
      @PathVariable UUID tenantId, @Valid @RequestBody CreateTenantAdminRequest request) {
    return service.createAdmin(tenantId, request);
  }

  @GetMapping("/policy")
  @Operation(summary = "Get global delivery policy")
  public PlatformPolicyResponse getPolicy() {
    return service.getPolicy();
  }

  @PutMapping("/policy")
  @Operation(summary = "Update global delivery policy")
  public PlatformPolicyResponse updatePolicy(@Valid @RequestBody PlatformPolicyRequest request) {
    return service.updatePolicy(request);
  }

  @GetMapping("/tenants/{tenantId}/rate-limit")
  @Operation(summary = "Get effective tenant rate limit")
  public TenantRateLimitResponse getRate(@PathVariable UUID tenantId) {
    return service.getRateLimit(tenantId);
  }

  @PutMapping("/tenants/{tenantId}/rate-limit")
  @Operation(summary = "Override tenant rate limit")
  public TenantRateLimitResponse updateRate(
      @PathVariable UUID tenantId, @Valid @RequestBody TenantRateLimitRequest request) {
    return service.updateRateLimit(tenantId, request);
  }
}
