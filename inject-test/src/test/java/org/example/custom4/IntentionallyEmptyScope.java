package org.example.custom4;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = BuildScope.class )
public @interface IntentionallyEmptyScope {
}
