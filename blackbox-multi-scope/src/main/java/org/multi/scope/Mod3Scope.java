package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(
  requires = Mod2Scope.class
)
public @interface Mod3Scope {}
