package org.example.custom4;

import io.avaje.inject.InjectModule;
import javax.inject.Scope;

@Scope
@InjectModule(requires = {MachineScope.class})
public @interface LinuxScope {
}
