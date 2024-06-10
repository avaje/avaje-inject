package org.example.myapp.events;

import org.example.external.aspect.MyExternalAspect;
import org.other.one.SomeOptionalDep;

import io.avaje.inject.Component;
import io.avaje.inject.events.Event;

@Component
public class ExternalEventPublisher {
  Event<SomeOptionalDep> event;

  public ExternalEventPublisher(Event<SomeOptionalDep> event) {

    this.event = event;
  }

  @MyExternalAspect
  public void fire(SomeOptionalDep dep) {
    event.fire(dep);
  }
}
