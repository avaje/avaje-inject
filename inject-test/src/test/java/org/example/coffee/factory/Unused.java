package org.example.coffee.factory;

import org.example.coffee.factory.other.Something;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class Unused {

  private final Something something;

  @Inject
  public Unused(Something something) {
    this.something = something;
  }

  public void doSomething() {
    something.doStuff();
  }
}
