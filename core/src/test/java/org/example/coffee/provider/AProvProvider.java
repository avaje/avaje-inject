package org.example.coffee.provider;

import org.example.coffee.grind.Grinder;

import javax.inject.Provider;
import javax.inject.Singleton;

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
