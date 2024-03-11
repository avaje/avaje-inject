package io.avaje.inject.spi;

import java.lang.reflect.Type;

import jakarta.inject.Provider;

/**
 * Provides late binding of Provider (like field/setter injection).
 */
final class ProviderPromise<T> implements Provider<T> {

  private final Type type;
  private final String name;
  private final DBuilder builder;
  private Provider<T> provider;

  ProviderPromise(Type type, String name, DBuilder builder) {
    this.type = type;
    this.name = name;
    this.builder = builder;
  }

  @Override
  public T get() {
    if (provider == null) {
      this.provider = builder.obtainProvider(type, name);
    }
    return provider.get();
  }
}
