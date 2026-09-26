package org.example.notification.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.notification.auth.api.dto.*;
import org.example.notification.auth.application.AuthenticationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {
  private final AuthenticationService authenticationService;

  public AuthController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate a platform or tenant administrator")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return authenticationService.login(request);
  }
}
