package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(automaticallyImport = ModCScope.class, strictWiring = true)
public @interface ModBScope {
}
