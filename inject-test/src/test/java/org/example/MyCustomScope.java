package org.example;

import io.avaje.inject.InjectModule;
import javax.inject.Scope;
import org.example.custom.LocalExternal;

@Scope
@InjectModule(requires = {System.class, LocalExternal.class})
public @interface MyCustomScope {
}
