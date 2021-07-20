package org.example;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = System.class)
public @interface MyCustomScope {
}
