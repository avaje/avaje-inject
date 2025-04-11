package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(provides = ModAScope.class)
public @interface ModAScope {}
