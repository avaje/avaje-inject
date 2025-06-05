package org.example.myapp.config;

import org.jspecify.annotations.Nullable;
import org.other.one.SomeOptionalDep;

import io.avaje.inject.Component;

@Component
public class SomeOptionalUser {

  final SomeOptionalDep optionalDep;

  public SomeOptionalUser(@Nullable SomeOptionalDep optionalDep) {
    this.optionalDep = optionalDep;
  }

  public boolean hasOptionalDependency() {
    return optionalDep != null;
  }
}
