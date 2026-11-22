package org.example.myapp.injector;

import io.avaje.inject.InjectorTarget;
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
}
