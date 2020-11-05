package org.example.coffee.provider;

import org.example.coffee.grind.Grinder;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class AProvProvider implements Provider<AProv> {

  private final Grinder grinder;

  AProvProvider(Grinder grinder) {
    this.grinder = grinder;
  }

  @Override
  public AProv get() {
    return new AProv(grinder);
  }
}
