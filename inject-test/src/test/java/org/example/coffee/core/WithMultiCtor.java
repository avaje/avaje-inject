package org.example.coffee.core;

import io.avaje.inject.Component;
import jakarta.inject.Inject;

@Component
public class WithMultiCtor {

  final Steamer steamer;

  public WithMultiCtor(Integer ignored) {
    this.steamer = null;
  }

  @Inject
  public WithMultiCtor(Steamer steamer) {
    this.steamer = steamer;
  }
}
