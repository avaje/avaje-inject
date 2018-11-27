package org.example.coffee.qualifier;

import javax.inject.Singleton;

@Blue
@Singleton
public class BlueStore implements SomeStore {

  @Override
  public String store() {
    return "blue";
  }
}
