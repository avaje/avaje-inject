package io.avaje.inject.spi;

import io.avaje.inject.BeanScope;

/**
 * Generated injector that supplies dependencies to an existing instance.
 *
 * @param <T> the target instance type
 */
public interface InstanceInjector<T> {

  /**
   * Injects dependencies into the given instance using the provided BeanScope.
   *
   * @param instance the instance to inject dependencies into
   * @param scope the BeanScope to use for injection
   */
  void inject(T instance, BeanScope scope);

  /** Return true if this injector registers a destruction callback for injected instances. */
  default boolean hasPreDestroy() {
    return false;
  }

  /** Return the destruction callback priority for injected instances. */
  default int preDestroyPriority() {
    return 1000;
  }

  /** Invoke the destruction callback for the given instance. */
  default void preDestroy(T instance) {}
}
