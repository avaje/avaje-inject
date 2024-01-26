package io.avaje.inject.events;

import io.avaje.inject.BeanScopeBuilder;
import io.avaje.inject.spi.Plugin;

public final class ObserverPlugin implements Plugin {

  @Override
  public Class<?>[] provides() {
    return new Class<?>[] {ObserverManager.class};
  }

  @Override
  public void apply(BeanScopeBuilder builder) {
    builder.provideDefault(ObserverManager.class, DObserverManager::new);
  }
}
