package io.avaje.inject.spi;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.inject.Provider;

/** Single instance Lazy Provider. {@link #get()} will return the same instance every time */
final class OnceProvider<T> implements Provider<T> {

  private final ReentrantLock lock = new ReentrantLock();
  private final Provider<T> provider;
  private T bean;

  OnceProvider(Provider<T> provider) {
    this.provider = Objects.requireNonNull(provider);
  }

  @Override
  public T get() {
    if (bean != null) {
      return bean;
    }
    lock.lock();
    try {
      if (bean == null) {
        bean = provider.get();
      }
      return bean;
    } finally {
      lock.unlock();
    }
  }
}
