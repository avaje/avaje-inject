package org.example.myapp.injector;

import java.util.Optional;
import java.util.Set;

import org.example.myapp.generic.Generic;
import org.jspecify.annotations.Nullable;

import io.avaje.inject.InjectorTarget;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/** Target using dependency types that are obtained differently from a BeanScope than a Builder. */
@InjectorTarget
public class DependencyKinds {

  @Inject Provider<InjectedThing> provider;
  @Inject Set<InjectedThing> set;
  @Inject Optional<InjectedThing> optional;
  @Inject Optional<Generic<String>> optionalGeneric;
  @Inject Optional<Generic<Long>> absentOptionalGeneric;
  @Inject @Nullable Generic<String> nullableGeneric;
  @Inject @Nullable Generic<Long> absentNullableGeneric;
  @Inject InjectedPrototype prototype;
  @Inject Provider<InjectedPrototype> prototypeProvider;
  Set<InjectedThing> methodSet;
  Provider<InjectedThing> methodProvider;
  Optional<Generic<String>> methodOptionalGeneric;
  @Nullable Generic<Long> methodAbsentNullableGeneric;

  @Inject
  void setAll(
      Set<InjectedThing> set,
      Provider<InjectedThing> provider,
      Optional<Generic<String>> optionalGeneric,
      @Nullable Generic<Long> absentNullableGeneric) {
    this.methodSet = set;
    this.methodProvider = provider;
    this.methodOptionalGeneric = optionalGeneric;
    this.methodAbsentNullableGeneric = absentNullableGeneric;
  }
}
