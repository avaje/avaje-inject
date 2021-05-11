package org.example.coffee.provider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
class FieldInjectProvider {

  @Inject
  Provider<AProv> aProvProvider;

  AProv testGet() {
    return aProvProvider.get();
  }
}
