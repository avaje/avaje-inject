package io.avaje.inject.generator.models.valid.lifecycle;

import java.util.function.Consumer;

import io.avaje.inject.BeanScope;
import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;

@Singleton
public class Minos {

  @PostConstruct
  void prepareThyself(Serpent serpent, Consumer<String> c, BeanScope b) {}

  //  @PreDestroy
  //  void thyEndIsNow() {
  //  }
}
