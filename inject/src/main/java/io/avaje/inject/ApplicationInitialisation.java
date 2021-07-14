package io.avaje.inject;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Register a callback that should be called when ApplicationScope is initialised.
 */
public class ApplicationInitialisation {

  private static final AtomicBoolean initialised = new AtomicBoolean();
  private static Consumer<BeanScopeBuilder> callback;

  /**
   * Register a callback that should be called when ApplicationScope is initialised.
   * <p>
   * This callback MUST be registered BEFORE ApplicationScope is used. So register
   * this callback very early in the application startup.
   * <p>
   * We use this callback to programmatically register externally provided dependencies.
   *
   * @param scopeBuilder The application scope BeanScopeBuilder
   */
  public static void register(Consumer<BeanScopeBuilder> scopeBuilder) {
    if (initialised.get()) {
      throw new IllegalStateException("register() being called after ApplicationScope has been initialised");
    }
    callback = scopeBuilder;
  }

  /**
   * Called as part of <code>ApplicationScope.init()</code>. The callback
   * must be registered before now or it will not be invoked.
   */
  static BeanScopeBuilder onInit(BeanScopeBuilder applicationScopeBuilder) {
    initialised.set(true);
    if (callback != null) {
      callback.accept(applicationScopeBuilder);
    }
    return applicationScopeBuilder;
  }
}
