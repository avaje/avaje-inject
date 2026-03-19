package org.example.myapp.lifecycle;

import io.avaje.inject.BeanScope;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Singleton;

@Singleton
public class ThrowingLifecycleWithScope {

  public boolean started;

  @PostConstruct
  void start(BeanScope b) throws Exception {
    started = true;
  }

  @PreDestroy
  void destroy(BeanScope b) throws Exception {}
}
