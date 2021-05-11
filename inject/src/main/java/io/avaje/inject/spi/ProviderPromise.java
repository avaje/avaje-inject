package io.avaje.inject.spi;

import javax.inject.Provider;
import java.util.function.Consumer;

/**
 * Provides late binding of Provider (like field/setter injection).
 */
class ProviderPromise<T> implements Provider<T>, Consumer<Builder> {

  private final Class<T> type;
  private final String name;
  private T bean;

  ProviderPromise(Class<T> type, String name) {
    this.type = type;
    this.name = name;
  }

  @Override
  public void accept(Builder builder) {
    this.bean = builder.get(type, name);
  }

  @Override
  public T get() {
    return bean;
  }

}
