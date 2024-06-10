package org.example.observes;

import io.avaje.inject.Component;
import io.avaje.inject.events.Event;
import jakarta.inject.Inject;

@Component
public class EventSender2 {

  @Inject
  @StrQualifier(value = "foo")
  Event<CustomEvent> event;
}
