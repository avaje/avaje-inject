package org.example.myapp.injector;

import io.avaje.inject.InjectorTarget;
import jakarta.inject.Inject;

/**
 * Base class shared by several externally-created subclasses, so it can be
 * annotated once instead of every concrete subclass.
 */
@InjectorTarget
public abstract class LegacyBase {

  private Dep1 dep1;

  @Inject
  void setDep1(Dep1 dep1) {
    this.dep1 = dep1;
  }

  public Dep1 dep1() {
    return dep1;
  }
}
