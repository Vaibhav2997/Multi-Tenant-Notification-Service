package org.example.notification.auth.infrastructure;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.example.notification.auth.application.JwtService;
import org.example.notification.auth.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserRepository users;

  public JwtAuthenticationFilter(JwtService jwtService, UserRepository users) {
    this.jwtService = jwtService;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      authenticate(header.substring(7));
    }
    chain.doFilter(request, response);
  }

  private void authenticate(String token) {
    try {
      Claims claims = jwtService.parse(token).getPayload();
      AppUser user = users.findById(UUID.fromString(claims.getSubject())).orElseThrow();
      if (!user.isActive()) return;
      CurrentUser principal =
          new CurrentUser(
              user.getId(),
              user.getRole(),
              user.getTenant() == null ? null : user.getTenant().getId());
      var authentication =
          new UsernamePasswordAuthenticationToken(
              principal,
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception ignored) {
      SecurityContextHolder.clearContext();
    }
  }
}
