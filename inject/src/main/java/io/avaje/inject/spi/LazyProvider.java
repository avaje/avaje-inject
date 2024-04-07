package io.avaje.inject.spi;

import jakarta.inject.Provider;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Wrap a Provider for lazy initialisation.
 */
final class LazyProvider<T> implements Provider<T> {

  private final ReentrantLock lock = new ReentrantLock();
  private final Provider<T> original;
  private T lazy;

  LazyProvider(Provider<T> original) {
    this.original = original;
  }

  @Override
  public T get() {
    if (lazy != null) {
      return lazy;
    }
    lock.lock();
    try {
      if (lazy == null) {
        lazy = original.get();
      }
      return lazy;
    } finally {
      lock.unlock();
    }
  }
}
