package org.example.notification.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.example.notification.tenant.domain.Tenant;

@Entity
@Table(name = "app_users")
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Tenant tenant;

  @Column(unique = true)
  private String email;

  private String passwordHash;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  private boolean active;
  private Instant createdAt;

  protected AppUser() {}

  public AppUser(Tenant tenant, String email, String passwordHash, UserRole role, Instant now) {
    this.tenant = tenant;
    this.email = email.toLowerCase();
    this.passwordHash = passwordHash;
    this.role = role;
    this.active = true;
    this.createdAt = now;
  }

  public UUID getId() {
    return id;
  }

  public Tenant getTenant() {
    return tenant;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public UserRole getRole() {
    return role;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
