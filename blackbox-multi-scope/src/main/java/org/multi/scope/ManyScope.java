package org.multi.scope;

import org.multi.crosscut.CrossCutModule;
import org.multi.modd.ModDModule;
import org.multi.mode.ModEModule;

import io.avaje.inject.InjectModule;
import jakarta.inject.Scope;

@Scope
@InjectModule(requires = {ModDModule.class, CrossCutModule.class, ModEModule.class})
public @interface ManyScope {}
