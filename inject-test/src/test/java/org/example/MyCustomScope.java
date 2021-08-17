package org.example;

import io.avaje.inject.InjectModule;
import javax.inject.Scope;
import org.example.custom.loc.LocalExternal;

@Scope
@InjectModule(requires = {LocalExternal.class})
public @interface MyCustomScope {
}
