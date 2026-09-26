package org.example.notification.delivery.application;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import org.example.notification.delivery.api.dto.*;
import org.springframework.stereotype.Component;

@Component
public class RequestHashService {
  public String hash(CreateNotificationBatchRequest request) {
    StringBuilder canonical =
        new StringBuilder()
            .append(request.getTemplateId())
            .append('|')
            .append(request.getScheduledAt())
            .append('|');
    appendMap(canonical, request.getVariables());
    for (RecipientRequest recipient : request.getRecipients()) {
      canonical.append('|').append(recipient.getAddress()).append('|');
      appendMap(canonical, recipient.getVariables());
    }
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }

  private void appendMap(StringBuilder target, Map<String, String> values) {
    if (values == null) return;
    new TreeMap<>(values)
        .forEach((key, value) -> target.append(key).append('=').append(value).append(';'));
  }
}
