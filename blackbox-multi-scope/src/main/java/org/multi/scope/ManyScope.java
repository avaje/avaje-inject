package org.multi.scope;

import org.other.one.custom.ExternalScope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(
    requires = {ExternalScope.class, ModDScope.class, CrossCutScope.class, ModEScope.class})
public @interface ManyScope {}
