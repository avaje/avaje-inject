package org.example.optional;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class OptBax {

  final OptionalService service;

  public OptBax(OptDefaultBax fallback, @Named("supplied") Optional<OptionalService> maybeService) {
    this.service = maybeService.orElse(fallback);
  }

  String hi() {
    return service.hi();
  }

}
