package io.avaje.inject.generator.models.valid.array;

import io.avaje.inject.Component;

@Component
public class Arrays {

  byte[] arr;

  public Arrays(byte[] arr) {
    this.arr = arr;
  }
}
