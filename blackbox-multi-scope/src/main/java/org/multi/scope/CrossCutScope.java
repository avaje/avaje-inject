package org.multi.scope;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(
    requires = {ModAScope.class, ModBScope.class},
    provides = CrossCutScope.class)
public @interface CrossCutScope {}
