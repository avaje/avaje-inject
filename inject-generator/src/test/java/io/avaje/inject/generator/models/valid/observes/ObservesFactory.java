package io.avaje.inject.generator.models.valid.observes;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.events.Observes;

@Factory
public class ObservesFactory {

  @Bean
  public ManagedLifecycleBean managedLifecycleBean() {
    return new ManagedLifecycleBean();
  }

  public void stopLifecycle(@Observes final CustomEvent e, final ManagedLifecycleBean bean) {
    bean.stop();
  }

  public static class ManagedLifecycleBean {
    public void stop() {}
  }
}
