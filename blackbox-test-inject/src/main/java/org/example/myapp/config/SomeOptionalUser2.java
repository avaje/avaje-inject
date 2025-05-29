package org.example.myapp.config;

import java.util.Optional;

import org.other.one.SomeOptionalDep;

import io.avaje.inject.Component;

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
