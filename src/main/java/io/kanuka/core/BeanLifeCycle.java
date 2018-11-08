package io.kanuka.core;

/**
 * Lifecycle of beans via <code>@PostConstruct</code> and <code>@PreDestroy</code>.
 */
public interface BeanLifeCycle {

  /**
   * Fire post construction.
   */
  void postConstruct();

  /**
   * Fire on shutdown or close of the bean context.
   */
  void preDestroy();
}
