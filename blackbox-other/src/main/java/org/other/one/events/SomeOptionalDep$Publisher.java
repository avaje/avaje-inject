package org.other.one.events;

import io.avaje.inject.Component;
import io.avaje.inject.event.Event;
import io.avaje.inject.event.ObserverManager;
import io.avaje.inject.spi.Generated;
import java.lang.reflect.Type;
import org.other.one.SomeOptionalDep;

@Component
@Generated("avaje-inject-generator")
public class SomeOptionalDep$Publisher extends Event<SomeOptionalDep> {

  private static final Type TYPE = SomeOptionalDep.class;

  public SomeOptionalDep$Publisher(ObserverManager manager) {
    super(manager, TYPE, "");
  }
}
