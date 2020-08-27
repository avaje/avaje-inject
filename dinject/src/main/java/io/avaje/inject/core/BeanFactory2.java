package io.avaje.inject.core;

/**
 * Factory for creating a bean given two request scope arguments.
 *
 * @param <T>  The type of bean to create
 * @param <R>  The first request argument
 * @param <R2> The second request argument
 */
public interface BeanFactory2<T, R, R2> {

  /**
   * Create and return the bean.
   */
  T create(R argument, R2 argument2);
}
