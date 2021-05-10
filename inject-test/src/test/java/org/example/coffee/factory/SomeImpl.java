package org.example.coffee.factory;

import org.example.coffee.factory.other.Something;

public class SomeImpl implements Something {

  @Override
  public String doStuff() {
    return "SomeImpl";
  }

  @Override
  public void otherThing() {

  }
}
