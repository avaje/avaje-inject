package org.example.coffee.factory;

import org.example.coffee.factory.other.Something;

import javax.inject.Singleton;

@Singleton
public class SomeImplBean implements Something {

  @Override
  public void doStuff() {

  }

  @Override
  public void otherThing() {

  }
}
