package org.example.myapp.injector;

import io.avaje.inject.InjectorTarget;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Inject;

@InjectorTarget(enablePreDestroy = true)
public class FailingDestroyTarget {

  @Inject InjectedThing dependency;

  @PreDestroy
  void destroy() {
    throw new IllegalStateException("destroy failed");
  }
}
