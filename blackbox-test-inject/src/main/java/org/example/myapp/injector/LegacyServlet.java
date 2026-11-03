package org.example.myapp.injector;

import io.avaje.inject.InjectorTarget;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Inject;

/**
 * Stand-in for a class some external framework instantiates itself, so
 * avaje-inject can't build it directly, but {@code @InjectorTarget} still gets
 * it a generated {@code Injector<LegacyServlet>} bean to inject into it.
 */
@InjectorTarget
public class LegacyServlet {

  private Dep1 dep1;
  private Dep2 dep2;
  private boolean destroyed;

  @Inject
  void setDep1(Dep1 dep1) {
    this.dep1 = dep1;
  }

  @Inject
  void setDep2(Dep2 dep2) {
    this.dep2 = dep2;
  }

  public Dep1 dep1() {
    return dep1;
  }

  public Dep2 dep2() {
    return dep2;
  }

  @PreDestroy
  void shutdown() {
    this.destroyed = true;
  }

  public boolean destroyed() {
    return destroyed;
  }
}
