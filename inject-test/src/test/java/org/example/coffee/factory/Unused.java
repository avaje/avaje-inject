package org.example.coffee.factory;

import org.example.coffee.factory.other.Something;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Unused {

  private final Something something;

  @Inject
  public Unused(Something something) {
    this.something = something;
  }

  public String doSomething() {
    return something.doStuff();
  }
}
