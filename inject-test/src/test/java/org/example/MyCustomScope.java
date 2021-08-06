package org.example;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;
import org.example.custom.LocalExternal;

@Scope
@InjectModule(requires = {LocalExternal.class})
public @interface MyCustomScope {
}
