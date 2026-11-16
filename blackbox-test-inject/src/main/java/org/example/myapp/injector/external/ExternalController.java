package org.example.myapp.injector.external;

import org.example.myapp.injector.Dep1;
import org.example.myapp.injector.Dep2;

import jakarta.inject.Inject;

/**
 * Stand-in for a type from an external jar that can't be annotated with
 * {@code @InjectorTarget}, so its injector is generated via {@code @InjectorTarget.Import}.
 */
public class ExternalController {

  @Inject
  public Dep1 dep1;

  private Dep2 dep2;

  @Inject
  public void setDep2(Dep2 dep2) {
    this.dep2 = dep2;
  }

  public Dep2 dep2() {
    return dep2;
  }
}
