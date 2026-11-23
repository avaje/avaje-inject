package org.example.myapp.injector;

import io.avaje.inject.InjectorTarget;
import jakarta.inject.Inject;

/** An annotated subclass of a target, its own injected members are included. */
@InjectorTarget(enablePreDestroy = true)
public class DerivedTarget extends ExternalInstance {

  @Inject InjectedPrototype ownDependency;

  public DerivedTarget() {
    super(null);
  }
}
