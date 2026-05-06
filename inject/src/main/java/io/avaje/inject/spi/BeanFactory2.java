package io.avaje.inject.spi;

/**
 * Factory for creating a bean given two request scope arguments.
 *
 * @param <T> The type of bean to create
 * @param <R> The first request argument
 * @param <R2> The second request argument
 * @deprecated use the latest version of avaje http for request scope support.
 */
@Deprecated
public interface BeanFactory2<T, R, R2> {

  /** Create and return the bean. */
  @Deprecated
  T create(R argument, R2 argument2);
}
