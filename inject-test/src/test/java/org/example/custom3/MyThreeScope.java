package org.example.custom3;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;
import org.example.custom2.OtherScope;

/**
 * This scope has a requires on another scope - requires = OtherScope
 */
@Scope
@InjectModule(requires = OtherScope.class)
public @interface MyThreeScope {
}
