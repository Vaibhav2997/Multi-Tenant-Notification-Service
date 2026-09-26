package org.example.notification.tenant.application;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.example.notification.auth.domain.*;
import org.example.notification.auth.infrastructure.UserRepository;
import org.example.notification.shared.api.*;
import org.example.notification.tenant.api.dto.*;
import org.example.notification.tenant.domain.*;
import org.example.notification.tenant.infrastructure.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PlatformAdministrationService {
  private final TenantRepository tenants;
  private final UserRepository users;
  private final ChannelConfigurationRepository channels;
  private final PlatformPolicyRepository policies;
  private final TenantRateLimitRepository rateLimits;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;

  public PlatformAdministrationService(
      TenantRepository tenants,
      UserRepository users,
      ChannelConfigurationRepository channels,
      PlatformPolicyRepository policies,
      TenantRateLimitRepository rateLimits,
      PasswordEncoder passwordEncoder,
      Clock clock) {
    this.tenants = tenants;
    this.users = users;
    this.channels = channels;
    this.policies = policies;
    this.rateLimits = rateLimits;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
  }

  @Transactional
  public TenantResponse createTenant(CreateTenantRequest request) {
    if (tenants.findBySlug(request.getSlug()).isPresent())
      throw new ConflictException(ApiErrorCode.CONFLICT, "Tenant slug already exists");
    Tenant tenant = tenants.save(new Tenant(request.getName(), request.getSlug(), clock.instant()));
    for (ChannelType channel : ChannelType.values())
      channels.save(new ChannelConfiguration(tenant, channel, clock.instant()));
    return toResponse(tenant);
  }

  public List<TenantResponse> listTenants() {
    return tenants.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional
  public TenantAdminResponse createAdmin(UUID tenantId, CreateTenantAdminRequest request) {
    Tenant tenant =
        tenants.findById(tenantId).orElseThrow(() -> new NotFoundException("Tenant not found"));
    if (users.findByEmailIgnoreCase(request.getEmail()).isPresent())
      throw new ConflictException(ApiErrorCode.CONFLICT, "Email already exists");
    AppUser user =
        users.save(
            new AppUser(
                tenant,
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                UserRole.TENANT_ADMIN,
                clock.instant()));
    return new TenantAdminResponse(user.getId(), user.getEmail(), tenant.getId());
  }

  public PlatformPolicyResponse getPolicy() {
    return policyResponse(requirePolicy());
  }

  @Transactional
  public PlatformPolicyResponse updatePolicy(PlatformPolicyRequest request) {
    if (request.getMaxRetryDelaySeconds() < request.getBaseRetryDelaySeconds())
      throw new IllegalArgumentException(
          "Maximum retry delay must be greater than or equal to base retry delay");
    PlatformPolicy policy = requirePolicy();
    policy.update(
        request.getDefaultRatePerMinute(),
        request.getMaxBatchSize(),
        request.getMaxAttempts(),
        request.getBaseRetryDelaySeconds(),
        request.getMaxRetryDelaySeconds(),
        clock.instant());
    return policyResponse(policy);
  }

  public TenantRateLimitResponse getRateLimit(UUID tenantId) {
    tenants.findById(tenantId).orElseThrow(() -> new NotFoundException("Tenant not found"));
    return rateLimits
        .findByTenantId(tenantId)
        .map(value -> new TenantRateLimitResponse(tenantId, value.getRequestsPerMinute(), true))
        .orElseGet(
            () ->
                new TenantRateLimitResponse(
                    tenantId, requirePolicy().getDefaultRatePerMinute(), false));
  }

  @Transactional
  public TenantRateLimitResponse updateRateLimit(UUID tenantId, TenantRateLimitRequest request) {
    Tenant tenant =
        tenants.findById(tenantId).orElseThrow(() -> new NotFoundException("Tenant not found"));
    TenantRateLimit limit =
        rateLimits
            .findByTenantId(tenantId)
            .orElseGet(
                () -> new TenantRateLimit(tenant, request.getRequestsPerMinute(), clock.instant()));
    limit.update(request.getRequestsPerMinute(), clock.instant());
    rateLimits.save(limit);
    return new TenantRateLimitResponse(tenantId, limit.getRequestsPerMinute(), true);
  }

  public PlatformPolicy requirePolicy() {
    return policies
        .findById(PlatformPolicy.SINGLETON_ID)
        .orElseThrow(() -> new IllegalStateException("Platform policy is not initialized"));
  }

  private TenantResponse toResponse(Tenant t) {
    return new TenantResponse(t.getId(), t.getName(), t.getSlug(), t.getCreatedAt());
  }

  private PlatformPolicyResponse policyResponse(PlatformPolicy p) {
    return new PlatformPolicyResponse(
        p.getDefaultRatePerMinute(),
        p.getMaxBatchSize(),
        p.getMaxAttempts(),
        p.getBaseRetryDelaySeconds(),
        p.getMaxRetryDelaySeconds());
  }
}
