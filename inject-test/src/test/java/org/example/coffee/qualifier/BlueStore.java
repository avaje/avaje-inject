package org.example.coffee.qualifier;

import org.example.coffee.qualifier.ColorStore.Color;

import jakarta.inject.Singleton;

@Singleton
@ColorStore(Color.BLUE)
public class BlueStore implements SomeStore {

  @Override
  public String store() {
    return "blue";
  }
}
