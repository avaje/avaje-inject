package org.example.coffee.provider;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
class FieldInjectProvider {

  @Inject
  Provider<AProv> aProvProvider;

  AProv testGet() {
    return aProvProvider.get();
  }
}
