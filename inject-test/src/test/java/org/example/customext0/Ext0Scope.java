package org.example.customext0;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = {Ext0iface.class, Ext0conc.class})
public @interface Ext0Scope {
}
