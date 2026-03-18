package io.avaje.inject.generator.models.valid.lifecycle;

import io.avaje.inject.BeanScope;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Singleton;

@Singleton
public class ThrowingLifecycleWithScope {
  @PostConstruct
  void start(BeanScope b) throws Exception {}

  @PreDestroy
  void destroy(BeanScope b) throws Exception {}
}
