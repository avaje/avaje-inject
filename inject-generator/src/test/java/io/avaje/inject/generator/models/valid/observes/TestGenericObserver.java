package io.avaje.inject.generator.models.valid.observes;

import java.util.List;

import io.avaje.inject.event.Observes;
import jakarta.inject.Singleton;

@Singleton
public class TestGenericObserver {

  void observe(@Observes List<String> e) {}
}
