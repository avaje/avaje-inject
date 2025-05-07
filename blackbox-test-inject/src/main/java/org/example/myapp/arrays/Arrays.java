package org.example.myapp.arrays;

import io.avaje.inject.Component;

@Component
public class Arrays {

  byte[] arr;

  public Arrays(byte[] arr) {
    this.arr = arr;
  }
}
