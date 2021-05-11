package org.example.coffee.provider;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
class MethodInjectProvider {

  private Provider<AProv> aProvProvider;

  private boolean emptyMethodInjection;

  @Inject
  void set(Provider<AProv> aProvProvider) {
    this.aProvProvider = aProvProvider;
  }

  @Inject
  void emptyMethod() {
    this.emptyMethodInjection = true;
  }

  public boolean isEmptyMethodInjection() {
    return emptyMethodInjection;
  }

  AProv testGet() {
    return aProvProvider.get();
  }
}
