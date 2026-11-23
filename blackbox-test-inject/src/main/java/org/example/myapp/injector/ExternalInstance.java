package org.example.myapp.injector;

import io.avaje.inject.BeanScope;
import io.avaje.inject.InjectorTarget;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Inject;

import java.util.List;

@InjectorTarget(enablePreDestroy = true)
public class ExternalInstance {

  @Inject InjectedThing fieldDependency;
  @Inject List<InjectedThing> dependencies;
  InjectedThing methodDependency;
  BeanScope injectedScope;
  BeanScope initializedScope;
  boolean initialized;
  int initializeCount;
  int destroyCount;

  public ExternalInstance(InjectedThing fieldDependency) {
    this.fieldDependency = fieldDependency;
  }

  @Inject
  void setMethodDependency(InjectedThing methodDependency) {
    this.methodDependency = methodDependency;
  }

  @Inject
  void setScope(BeanScope scope) {
    this.injectedScope = scope;
  }

  @PostConstruct
  void initialize(BeanScope scope) {
    initialized = true;
    initializeCount++;
    initializedScope = scope;
  }

  @PreDestroy
  void destroy() {
    destroyCount++;
  }
}
