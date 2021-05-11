package org.example.coffee.factory;

import io.avaje.inject.Secondary;
import org.example.coffee.factory.other.Something;

import javax.inject.Singleton;

@Secondary
@Singleton
public class SomeImplBean implements Something {

  @Override
  public String doStuff() {
    return "SomeImplBean";
  }

  @Override
  public void otherThing() {

  }
}
