package org.example.coffee.circular;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CircC {

  // to handle circular dependency we need to make one of
  // the dependencies use field injection rather than
  // constructor injection
//  @Inject
//  CircA circA;

  private final CircA circA;

  public CircC(CircA circA) {
    this.circA = circA;
  }
}
