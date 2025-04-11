package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(automaticallyImport = {ModDScope.class, CrossCutScope.class, ModEScope.class}, strictWiring = true)
public @interface ManyScope {
}
