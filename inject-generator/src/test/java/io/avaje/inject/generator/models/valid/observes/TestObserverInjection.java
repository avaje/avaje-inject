package io.avaje.inject.generator.models.valid.observes;

import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Singleton
public class TestObserverInjection {

  void observe(@Observes(async = true) String e, TestObserver observer) {}
}
