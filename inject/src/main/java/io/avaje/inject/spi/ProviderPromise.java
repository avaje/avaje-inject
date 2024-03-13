package io.avaje.inject.spi;

import jakarta.inject.Provider;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Provides late binding of Provider (like field/setter injection).
 */
final class ProviderPromise<T> implements Provider<T>, Consumer<Builder> {

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
  public void accept(Builder _builder) {
    this.provider = builder.obtainProvider(type, name);
  }

  @Override
  public T get() {
    if (provider == null) {
      throw new IllegalStateException("Illegal to call Provider.get() method during DI wiring. " +
        "Look to use @PostConstruct or perhaps use java.util.function.Supplier");
    }
    return provider.get();
  }

}
