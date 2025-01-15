package io.avaje.inject.generator.models.valid.provider;

public class BProv<T> {

  final T value;

  public BProv(T value) {
    this.value = value;
  }

  public T get() {
    return value;
  }
}
