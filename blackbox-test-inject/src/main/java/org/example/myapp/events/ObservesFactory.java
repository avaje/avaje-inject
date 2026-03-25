package org.example.myapp.events;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.events.Event;
import io.avaje.inject.events.Observes;

@Factory
public class ObservesFactory {

  @Bean
  public ManagedLifecycle managedLifecycle(Event<StopEvent> event) {
    return new ManagedLifecycle(event);
  }

  public void onStop(@Observes StopEvent event, ManagedLifecycle bean) {
    bean.stop();
  }

  public static class StopEvent {}

  public static class ManagedLifecycle {
    private final Event<StopEvent> event;

    public ManagedLifecycle(Event<StopEvent> event) {
      this.event = event;
    }

    public void fire() {
      event.fire(new StopEvent());
    }

    private boolean stopped;

    public void stop() {
      this.stopped = true;
    }

    public boolean isStopped() {
      return stopped;
    }
  }
}
