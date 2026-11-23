package org.example.myapp.injector;

import java.util.Optional;
import java.util.Set;

import io.avaje.inject.InjectorTarget;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/** Target using dependency types that are obtained differently from a BeanScope than a Builder. */
@InjectorTarget
public class DependencyKinds {

  @Inject Provider<InjectedThing> provider;
  @Inject Set<InjectedThing> set;
  @Inject Optional<InjectedThing> optional;
  @Inject InjectedPrototype prototype;
  @Inject Provider<InjectedPrototype> prototypeProvider;
  Set<InjectedThing> methodSet;
  Provider<InjectedThing> methodProvider;

  @Inject
  void setAll(Set<InjectedThing> set, Provider<InjectedThing> provider) {
    this.methodSet = set;
    this.methodProvider = provider;
  }
}
