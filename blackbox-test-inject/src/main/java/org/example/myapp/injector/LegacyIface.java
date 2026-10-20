package org.example.myapp.injector;

import io.avaje.inject.InjectorTarget;
import jakarta.inject.Inject;

/** Common interface implemented by many externally-created instances. */
@InjectorTarget
public interface LegacyIface {

  @Inject
  void setDep2(Dep2 dep2);
}
