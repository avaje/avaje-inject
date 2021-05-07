package org.example.coffee.provider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

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
