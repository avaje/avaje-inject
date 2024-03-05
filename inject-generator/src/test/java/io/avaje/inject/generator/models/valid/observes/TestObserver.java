package io.avaje.inject.generator.models.valid.observes;

import io.avaje.inject.event.Observes;
import jakarta.inject.Singleton;

@Singleton
public class TestObserver {

  void observe(@Observes CustomEvent e) {}
}
