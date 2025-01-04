package io.avaje.inject.spi;

import io.avaje.inject.BeanScope;

/** Hook to run an action during beanScope closing. */
@FunctionalInterface
public interface PreDestroyHook {

  /**
   * Run an action to cleanup a bean during beanScope closing.
   *
   * @param beanScope the current bean scope
   */
  void destroy(BeanScope beanScope) throws Exception;
}
