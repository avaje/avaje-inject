package org.multi.scope;

import org.multi.modc.ModCModule;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = ModCModule.class)
public @interface ModBScope {}
