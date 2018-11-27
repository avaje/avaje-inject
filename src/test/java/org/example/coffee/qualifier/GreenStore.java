package org.example.coffee.qualifier;

import javax.inject.Singleton;

@Singleton
public class GreenStore implements SomeStore {
  @Override
  public String store() {
    return "green";
  }
}
