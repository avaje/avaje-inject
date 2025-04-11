package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(provides = ModEScope.class)
public @interface ModEScope {}
