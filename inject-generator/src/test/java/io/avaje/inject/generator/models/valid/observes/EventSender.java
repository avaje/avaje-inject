package io.avaje.inject.generator.models.valid.observes;

import java.util.List;

import io.avaje.inject.events.Event;
import io.avaje.inject.generator.models.valid.qualifier.TempQualifier;
import io.avaje.inject.generator.models.valid.qualifier.TempQualifier.Scale;
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
      @TempQualifier(value = Scale.CELSIUS, someOtherString = "celsi") Event<CustomEvent> celsius) {
    this.event = event;
  }

  @Inject
  public void methodInject(@Named("method") Event<String> celsius) {}
}
