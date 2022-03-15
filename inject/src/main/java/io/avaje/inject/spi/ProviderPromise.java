package io.avaje.inject.spi;

import javax.inject.Provider;
import java.util.function.Consumer;

/**
 * Provides late binding of Provider (like field/setter injection).
 */
final class ProviderPromise<T> implements Provider<T>, Consumer<Builder> {

  private final Class<T> type;
  private final String name;
  private final DBuilder builder;
  private Provider<T> provider;

  ProviderPromise(Class<T> type, String name, DBuilder builder) {
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
    return provider.get();
  }

}
