package org.example.notification.tenant.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.example.notification.auth.domain.CurrentUser;
import org.example.notification.tenant.api.dto.*;
import org.example.notification.tenant.application.*;
import org.example.notification.tenant.domain.ChannelType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenant/channels")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tenant channels")
public class TenantChannelController {
  private final TenantAccessService access;
  private final ChannelConfigurationService channels;

  public TenantChannelController(TenantAccessService access, ChannelConfigurationService channels) {
    this.access = access;
    this.channels = channels;
  }

  @GetMapping
  @Operation(summary = "List channel configurations")
  public List<ChannelConfigurationResponse> list(@AuthenticationPrincipal CurrentUser user) {
    return channels.list(access.requireTenant(user).getId());
  }

  @PutMapping("/{channel}")
  @Operation(summary = "Configure a notification channel")
  public ChannelConfigurationResponse update(
      @AuthenticationPrincipal CurrentUser user,
      @PathVariable ChannelType channel,
      @Valid @RequestBody ChannelConfigurationRequest request) {
    return channels.update(access.requireTenant(user).getId(), channel, request);
  }
}
