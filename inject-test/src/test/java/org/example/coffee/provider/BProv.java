package org.example.coffee.provider;

public class BProv<T> {

  final T value;

  public BProv(T value) {
    this.value = value;
  }

  public T get() {
    return value;
  }
}
