package io.avaje.inject.generator.models.valid.observes;

import java.util.List;

import io.avaje.inject.event.Observes;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class TestQualifiedObserver {

  void observe(@Observes @Named("list") List<String> e) {}
}
