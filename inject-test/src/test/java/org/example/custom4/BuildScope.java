package org.example.custom4;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = Build.class )
public @interface BuildScope {
}
