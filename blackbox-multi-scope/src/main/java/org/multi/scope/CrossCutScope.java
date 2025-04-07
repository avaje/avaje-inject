package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;
import org.multi.modb.BeanInModB;

@Scope
@InjectModule(requires = {ModAScope.class, ModBScope.class}, strictWiring = true)
public @interface CrossCutScope {
}
