package org.multi.scope;

import org.multi.moda.ModAModule;
import org.multi.modb.ModBModule;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = {ModAModule.class, ModBModule.class})
public @interface CrossCutScope {}
