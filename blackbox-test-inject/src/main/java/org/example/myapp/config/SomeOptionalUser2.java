package org.example.myapp.config;

import io.avaje.inject.Component;
import io.avaje.lang.Nullable;

import java.util.Optional;

import org.other.one.SomeOptionalDep;

@Component
public class SomeOptionalUser2 {

  final SomeOptionalDep optionalDep;

  public SomeOptionalUser2(Optional<SomeOptionalDep> optionalDep) {
    this.optionalDep = optionalDep.orElse(null);
  }

  public boolean hasOptionalDependency() {
    return optionalDep != null;
  }
}
