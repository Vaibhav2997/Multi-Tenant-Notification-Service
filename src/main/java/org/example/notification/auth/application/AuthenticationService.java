package org.example.notification.auth.application;

import org.example.notification.auth.api.dto.*;
import org.example.notification.auth.domain.AppUser;
import org.example.notification.auth.infrastructure.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthenticationService(
      UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public LoginResponse login(LoginRequest request) {
    AppUser user =
        users
            .findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    if (!user.isActive()
        || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new BadCredentialsException("Invalid email or password");
    }
    return new LoginResponse(
        jwtService.issue(user),
        user.getRole().name(),
        user.getTenant() == null ? null : user.getTenant().getId());
  }
}
