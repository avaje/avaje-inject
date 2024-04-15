package org.example.coffee.provider;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
class ProtoTypeNumberGetter {

  private final Provider<Long> nProv;

  ProtoTypeNumberGetter(Provider<Long> nProv) {
    this.nProv = nProv;
  }

  Long number() {
    return nProv.get();
  }
}
