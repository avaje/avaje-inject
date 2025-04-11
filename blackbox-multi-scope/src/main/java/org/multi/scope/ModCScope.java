package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(provides = ModCScope.class)
public @interface ModCScope {}
