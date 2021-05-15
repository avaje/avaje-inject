package io.avaje.inject;

/**
 * Provides request scoped beans.
 */
public interface RequestScopeProvider<T> {

  /**
   * Create and return the request scope bean instance.
   *
   * @param scope The request scope when creating the bean
   * @return The request scoped bean instance
   */
  <T> T provide(RequestScope scope);

}
