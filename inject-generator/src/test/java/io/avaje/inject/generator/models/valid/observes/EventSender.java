package io.avaje.inject.generator.models.valid.observes;

import java.util.List;

import io.avaje.inject.event.Event;
import io.avaje.inject.spi.GenericType;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class EventSender {
  private final Event<CustomEvent> event;

  @Inject Event<List<String>> fieldInject;

  public EventSender(
      @Named("same") Event<CustomEvent> event,
      @Named("same") Event<CustomEvent> dup,
      @Named("different") Event<CustomEvent> celsius) {
    this.event = event;
  }

  @Inject
  public void methodInject(@Named("method") Event<String> celsius) {}
}
