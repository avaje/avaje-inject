package org.example.coffee.provider;

import io.avaje.inject.Component;
import jakarta.inject.Provider;

@Component
class BProvUser {

  private final Provider<BProv<String>> provider;

  BProvUser(Provider<BProv<String>> provider){
    this.provider = provider;
  }

  Provider<BProv<String>> getProvider() {
    return provider;
  }
}
