package org.example.custom4;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = {Machine.class, BuildScope.class})
public @interface MachineScope {
}
