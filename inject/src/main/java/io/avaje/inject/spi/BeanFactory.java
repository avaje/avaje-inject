package io.avaje.inject.spi;

/**
 * Factory for creating a bean given a single request scope argument.
 *
 * @param <T> The type of bean to create
 * @param <R> The request argument
 *
 * @deprecated use the latest version of avaje http for request scope support.
 */
@Deprecated(forRemoval = true)
public interface BeanFactory<T, R> {

  /**
   * Create and return the bean.
   */
  @Deprecated(forRemoval = true)
  T create(R argument);
}
