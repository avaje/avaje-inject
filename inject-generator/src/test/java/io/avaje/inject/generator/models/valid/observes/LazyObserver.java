package io.avaje.inject.generator.models.valid.observes;

import io.avaje.inject.Lazy;
import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Lazy
@Singleton
public class LazyObserver {

  public void observe(@Observes CustomEvent event, EventSender sender) {}
}
