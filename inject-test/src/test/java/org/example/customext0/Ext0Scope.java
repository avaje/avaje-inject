package org.example.customext0;

import io.avaje.inject.InjectModule;
import javax.inject.Scope;

@Scope
@InjectModule(requires = {Ext0iface.class, Ext0conc.class})
public @interface Ext0Scope {
}
