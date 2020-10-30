package io.avaje.inject.spi;

/**
 * Lifecycle of beans via <code>@PostConstruct</code> and <code>@PreDestroy</code>.
 */
public interface BeanLifecycle {

  /**
   * Fire post construction.
   */
  void postConstruct();

  /**
   * Fire on shutdown or close of the bean context.
   */
  void preDestroy();
}
