package io.avaje.inject.spi;

/**
 * Factory for creating a bean given a single request scope argument.
 *
 * @param <T> The type of bean to create
 * @param <R> The request argument
 */
public interface BeanFactory<T, R> {

  /**
   * Create and return the bean.
   */
  T create(R argument);
}
