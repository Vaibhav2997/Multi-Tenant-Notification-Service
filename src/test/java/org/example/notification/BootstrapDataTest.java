package org.example.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.Optional;
import org.example.notification.auth.domain.*;
import org.example.notification.auth.infrastructure.UserRepository;
import org.example.notification.tenant.domain.PlatformPolicy;
import org.example.notification.tenant.infrastructure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class BootstrapDataTest {
  private final UserRepository users = mock(UserRepository.class);
  private final PlatformPolicyRepository policies = mock(PlatformPolicyRepository.class);
  private final PasswordEncoder encoder = mock(PasswordEncoder.class);
  private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

  @BeforeEach
  void platformPolicyExists() {
    when(policies.findById(PlatformPolicy.SINGLETON_ID))
        .thenReturn(Optional.of(new PlatformPolicy(clock.instant())));
  }

  @Test
  void acceptsExistingMatchingPlatformAdministrator() {
    AppUser existing = platformAdmin("stored-hash");
    when(users.findByEmailIgnoreCase("platform@example.com")).thenReturn(Optional.of(existing));
    when(encoder.matches("configured-password", "stored-hash")).thenReturn(true);

    assertDoesNotThrow(() -> bootstrap().run());
    verify(users, never()).save(any());
  }

  @Test
  void rejectsExistingAdministratorWhenPasswordDoesNotMatch() {
    AppUser existing = platformAdmin("stored-hash");
    when(users.findByEmailIgnoreCase("platform@example.com")).thenReturn(Optional.of(existing));
    when(encoder.matches("configured-password", "stored-hash")).thenReturn(false);

    assertThrows(IllegalStateException.class, () -> bootstrap().run());
  }

  @Test
  void rejectsExistingEmailThatIsNotAPlatformAdministrator() {
    AppUser tenantAdmin =
        new AppUser(
            null, "platform@example.com", "stored-hash", UserRole.TENANT_ADMIN, clock.instant());
    when(users.findByEmailIgnoreCase("platform@example.com")).thenReturn(Optional.of(tenantAdmin));
    when(encoder.matches("configured-password", "stored-hash")).thenReturn(true);

    assertThrows(IllegalStateException.class, () -> bootstrap().run());
  }

  private BootstrapData bootstrap() {
    return new BootstrapData(
        users, policies, encoder, clock, "platform@example.com", "configured-password");
  }

  private AppUser platformAdmin(String hash) {
    return new AppUser(
        null, "platform@example.com", hash, UserRole.PLATFORM_ADMIN, clock.instant());
  }
}
