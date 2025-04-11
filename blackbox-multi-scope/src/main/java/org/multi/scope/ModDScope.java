package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = {ModAScope.class}, strictWiring = true)
public @interface ModDScope {

}
