package io.avaje.inject.generator.models.valid.lifecycle;

import java.util.function.Consumer;

import io.avaje.inject.BeanScope;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.Prototype;

@Prototype
public class Serpent {

  @PostConstruct
  void hiss(Consumer<String> c, BeanScope b) {}

}
