package org.example.custom4;

import io.avaje.inject.InjectModule;
import javax.inject.Scope;

@Scope
@InjectModule(requires = {Machine.class, IntentionallyEmptyScope.class})
public @interface MachineScope {
}
