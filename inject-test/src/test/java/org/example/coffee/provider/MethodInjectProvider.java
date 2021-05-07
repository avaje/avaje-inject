package org.example.coffee.provider;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
class MethodInjectProvider {

  private Provider<AProv> aProvProvider;

  @Inject
  void set(Provider<AProv> aProvProvider) {
    this.aProvProvider = aProvProvider;
  }

  AProv testGet() {
    return aProvProvider.get();
  }
}
