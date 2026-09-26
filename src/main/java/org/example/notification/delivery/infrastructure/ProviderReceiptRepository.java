package org.example.notification.delivery.infrastructure;

import java.util.*;
import org.example.notification.delivery.domain.ProviderReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderReceiptRepository extends JpaRepository<ProviderReceipt, UUID> {
  Optional<ProviderReceipt> findByProviderIdempotencyKey(String key);
}
