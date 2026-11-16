package org.example.myapp.injector;

import org.example.myapp.injector.external.ExternalController;

import io.avaje.inject.InjectorTarget;

@InjectorTarget.Import(ExternalController.class)
class ImportInjectors {}
