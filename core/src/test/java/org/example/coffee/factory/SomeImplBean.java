package org.example.coffee.factory;

import io.dinject.annotation.Secondary;
import org.example.coffee.factory.other.Something;

import javax.inject.Singleton;

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
