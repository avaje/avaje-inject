package io.avaje.inject.generator.models.valid.provider;

import java.util.function.Supplier;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class ProviderUser {

  Provider<Grinder> grinder;
  Provider<Supplier<String>> supplier;

  public ProviderUser(Provider<Grinder> grinder, Provider<Supplier<String>> supplier) {
    this.grinder = grinder;
    this.supplier = supplier;
  }
}
