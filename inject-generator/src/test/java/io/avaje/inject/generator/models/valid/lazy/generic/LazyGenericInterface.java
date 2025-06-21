package io.avaje.inject.generator.models.valid.lazy.generic;

public interface LazyGenericInterface<T> {

  void something();

  String somethingElse();

  T gen();
}
