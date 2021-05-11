package org.example.coffee.factory;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class MultipleOtherThings {

  private final Otherthing blue;
  private final Otherthing red;
  private final Otherthing green;
  private final Otherthing yellow;

  MultipleOtherThings(@Named("blue") Otherthing blue, @Named("red") Otherthing red, @Named("green") Otherthing green, @Named("yellow") Otherthing yellow) {
    this.blue = blue;
    this.red = red;
    this.green = green;
    this.yellow = yellow;
  }

  public String blue() {
    return blue.doOther();
  }

  public String red() {
    return red.doOther();
  }

  public String green() {
    return green.doOther();
  }

  public String yellow() {
    return yellow.doOther();
  }
}
