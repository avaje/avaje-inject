package org.example.coffee.factory;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class TwoOtherThings {

  private final Otherthing blue;
  private final Otherthing red;

  TwoOtherThings(@Named("blue") Otherthing blue, @Named("red") Otherthing red) {
    this.blue = blue;
    this.red = red;
  }

  public String blue() {
    return blue.doOther();
  }

  public String red() {
    return red.doOther();
  }
}
