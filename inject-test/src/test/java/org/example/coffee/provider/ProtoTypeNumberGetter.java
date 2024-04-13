package org.example.coffee.provider;

import org.example.coffee.grind.Grinder;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class ProtoTypeNumberGetter {

  private final Provider<Integer> nProv;

  public ProtoTypeNumberGetter(Provider<Integer> nProv) {
    this.nProv = nProv;
  }

  Integer number() {
    return nProv.get();
  }
}
