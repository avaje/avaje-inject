package org.multi.scope;

import org.multi.moda.ModAModule;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = {ModAModule.class})
public @interface ModDScope {}
