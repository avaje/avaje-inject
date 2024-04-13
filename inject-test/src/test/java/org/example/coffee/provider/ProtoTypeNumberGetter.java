package org.example.coffee.provider;

import org.example.coffee.grind.Grinder;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class ProtoTypeNumberGetter {

  private final Provider<Long> nProv;

  public ProtoTypeNumberGetter(Provider<Long> nProv) {
    this.nProv = nProv;
  }

  Long number() {
    return nProv.get();
  }
}
