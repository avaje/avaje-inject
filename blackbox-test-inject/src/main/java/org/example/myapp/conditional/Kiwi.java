package org.example.myapp.conditional;

import io.avaje.inject.RequiresProperty;
import jakarta.inject.Singleton;

@Singleton
@RequiresProperty("kiwi")
public class Kiwi implements Bird, Fruit {

  @Override
  public String toString() {
    return "Kiwi";
  }
}
