package org.example.notification.delivery.infrastructure;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class DispatchConfiguration {
  @Bean("notificationExecutor")
  public ThreadPoolTaskExecutor notificationExecutor(
      @Value("${notification.dispatch.workers}") int workers,
      @Value("${notification.dispatch.queue-capacity}") int capacity) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(workers);
    executor.setMaxPoolSize(workers);
    executor.setQueueCapacity(capacity);
    executor.setThreadNamePrefix("notification-worker-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    executor.initialize();
    return executor;
  }
}
