package io.avaje.inject;

/**
 * Builder for RequestScope.
 * <p>
 * When building a request scope we can provide additional instances that can then we wired as
 * dependencies to request scoped beans.
 */
public interface RequestScopeBuilder {

  /**
   * Provide a bean that can be dependency of a request scoped bean.
   *
   * @param type The type of provided bean
   * @param bean The instance being provided
   * @return The builder
   */
  <D> RequestScopeBuilder withBean(Class<D> type, D bean);

  /**
   * Provide a bean that can be dependency of a request scoped bean.
   *
   * @param name The qualifier name of the provided bean
   * @param type The type of provided bean
   * @param bean The instance being provided
   * @return The builder
   */
  <D> RequestScopeBuilder withBean(String name, Class<D> type, D bean);

  /**
   * Build and return the request scope.
   */
  RequestScope build();
}
