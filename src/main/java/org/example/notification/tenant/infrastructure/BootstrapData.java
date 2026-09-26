package org.example.notification.tenant.infrastructure;

import java.time.Clock;
import org.example.notification.auth.domain.*;
import org.example.notification.auth.infrastructure.UserRepository;
import org.example.notification.tenant.domain.PlatformPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapData implements CommandLineRunner {
  private final UserRepository users;
  private final PlatformPolicyRepository policies;
  private final PasswordEncoder encoder;
  private final Clock clock;
  private final String email;
  private final String password;

  public BootstrapData(
      UserRepository users,
      PlatformPolicyRepository policies,
      PasswordEncoder encoder,
      Clock clock,
      @Value("${notification.bootstrap.admin-email}") String email,
      @Value("${notification.bootstrap.admin-password}") String password) {
    this.users = users;
    this.policies = policies;
    this.encoder = encoder;
    this.clock = clock;
    this.email = email;
    this.password = password;
  }

  @Override
  public void run(String... args) {
    policies
        .findById(PlatformPolicy.SINGLETON_ID)
        .orElseGet(() -> policies.save(new PlatformPolicy(clock.instant())));
    users
        .findByEmailIgnoreCase(email)
        .ifPresentOrElse(this::validateExistingAdmin, this::createAdmin);
  }

  private void validateExistingAdmin(AppUser user) {
    boolean validPlatformIdentity =
        user.isActive() && user.getRole() == UserRole.PLATFORM_ADMIN && user.getTenant() == null;
    boolean validPassword = encoder.matches(password, user.getPasswordHash());
    if (!validPlatformIdentity || !validPassword) {
      throw new IllegalStateException(
          "Existing bootstrap account does not match the configured platform administrator");
    }
  }

  private void createAdmin() {
    users.save(
        new AppUser(
            null, email, encoder.encode(password), UserRole.PLATFORM_ADMIN, clock.instant()));
  }
}
