package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(
  requires = {
    Mod1Scope.class,
    Mod3Scope.class
  }
)
public @interface Mod4Scope {}
