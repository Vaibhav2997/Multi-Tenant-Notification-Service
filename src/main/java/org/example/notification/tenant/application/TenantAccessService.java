package org.example.notification.tenant.application;

import org.example.notification.auth.domain.CurrentUser;
import org.example.notification.shared.api.NotFoundException;
import org.example.notification.tenant.domain.Tenant;
import org.example.notification.tenant.infrastructure.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantAccessService {
  private final TenantRepository tenants;

  public TenantAccessService(TenantRepository tenants) {
    this.tenants = tenants;
  }

  public Tenant requireTenant(CurrentUser user) {
    if (user == null || user.getTenantId() == null) throw new NotFoundException("Tenant not found");
    return tenants
        .findById(user.getTenantId())
        .orElseThrow(() -> new NotFoundException("Tenant not found"));
  }
}
