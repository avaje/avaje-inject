package org.example.coffee.qualifier;

import jakarta.inject.Singleton;

@Green
@Singleton
public class GreenStore implements SomeStore {
  @Override
  public String store() {
    return "green";
  }
}
