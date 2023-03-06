package org.example.myapp.config;

import io.avaje.inject.Component;
import io.avaje.lang.Nullable;

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
