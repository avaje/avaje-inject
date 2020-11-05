package org.example.coffee.factory;

import io.avaje.inject.Secondary;
import org.example.coffee.factory.other.Something;

import jakarta.inject.Singleton;

@Secondary
@Singleton
public class SomeImplBean implements Something {

  @Override
  public void doStuff() {

  }

  @Override
  public void otherThing() {

  }
}
