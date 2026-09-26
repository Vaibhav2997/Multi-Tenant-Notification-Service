package org.example.notification.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import javax.crypto.SecretKey;
import org.example.notification.auth.domain.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long ttlMinutes;
  private final Clock clock;

  public JwtService(
      @Value("${notification.jwt.secret}") String secret,
      @Value("${notification.jwt.ttl-minutes}") long ttlMinutes,
      Clock clock) {
    this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    this.ttlMinutes = ttlMinutes;
    this.clock = clock;
  }

  public String issue(AppUser user) {
    Instant now = clock.instant();
    JwtBuilder builder =
        Jwts.builder()
            .subject(user.getId().toString())
            .claim("role", user.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(Duration.ofMinutes(ttlMinutes))));
    if (user.getTenant() != null) {
      builder.claim("tenantId", user.getTenant().getId().toString());
    }
    return builder.signWith(key).compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
  }
}
