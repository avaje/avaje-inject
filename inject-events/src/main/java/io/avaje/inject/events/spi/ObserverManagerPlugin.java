package io.avaje.inject.events.spi;

import io.avaje.inject.BeanScopeBuilder;
import io.avaje.spi.ServiceProvider;

/** Plugin for avaje inject that provides a default Jsonb instance. */
@ServiceProvider
public final class ObserverManagerPlugin implements io.avaje.inject.spi.InjectPlugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[] {DObserverManager.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(null, DObserverManager.class, DObserverManager::new);
  }
}
