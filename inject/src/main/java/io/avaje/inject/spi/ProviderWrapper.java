package io.avaje.inject.spi;

import jakarta.inject.Provider;

/**
 * Just wrap a bean.
 */
class ProviderWrapper<T> implements Provider<T> {

  private T bean;

  ProviderWrapper(T bean) {
    this.bean = bean;
  }

  @Override
  public T get() {
    return bean;
  }

}
