package org.example.myapp.lifecycle;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Singleton;

@Singleton
public class ThrowingLifecycle {

  public boolean started;

  @PostConstruct
  void start() throws Exception {
    started = true;
  }

  @PreDestroy
  void destroy() throws Exception {}
}
