package org.other.one.events;

import java.lang.reflect.Type;

import org.other.one.SomeOptionalDep;

import io.avaje.inject.Component;
import io.avaje.inject.events.Event;
import io.avaje.inject.events.ObserverManager;
import io.avaje.inject.spi.Generated;

@Component
@Generated("avaje-inject-generator")
public class SomeOptionalDep_Publisher extends Event<SomeOptionalDep> {

  private static final Type TYPE = SomeOptionalDep.class;

  public SomeOptionalDep_Publisher(ObserverManager manager) {
    super(manager, TYPE, "");
  }
}
