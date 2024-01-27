package io.avaje.inject.generator.models.valid.observes;

import java.util.List;

import io.avaje.inject.events.Observes;
import jakarta.inject.Singleton;

@Singleton
public class TestQualifiedObserver {

  void observe(@Observes List<String> e) {}
}
