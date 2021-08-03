package org.example.customext1;

import io.avaje.inject.InjectModule;
import javax.inject.Scope;
import org.example.customext0.Ext0Scope;

@Scope
@InjectModule(requires = {Ext0Scope.class, Ext1iface.class, Ext1conc.class})
public @interface Ext1Scope {
}
