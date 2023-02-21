package org.example;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.example.custom.loc.LocalExternal;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@Retention(RetentionPolicy.RUNTIME)
@InjectModule(requires = {LocalExternal.class})
public @interface MyCustomScope {
}
