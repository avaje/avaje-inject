package org.example.coffee.qualifier;

import jakarta.inject.Singleton;

// no qualifier on this one
@Singleton
public class NoNameStore implements SomeStore {

  @Override
  public String store() {
    return "noName";
  }

}
