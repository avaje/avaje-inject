package io.avaje.inject.generator.models.valid.lifecycle;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Singleton;

@Singleton
public class ThrowingLifecycle {
  @PostConstruct
  void start() throws Exception {}

  @PreDestroy
  void destroy() throws Exception {}
}
