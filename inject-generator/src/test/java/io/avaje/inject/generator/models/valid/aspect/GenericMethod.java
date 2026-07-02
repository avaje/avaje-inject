package io.avaje.inject.generator.models.valid.aspect;

public interface GenericMethod {

  <T> T doIt(GenericMethodHolder<T> holder);

  class GenericMethodHolder<T> {
    T value;
  }
}
