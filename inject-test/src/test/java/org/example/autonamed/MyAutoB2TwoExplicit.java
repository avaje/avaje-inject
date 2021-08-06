package org.example.autonamed;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class MyAutoB2TwoExplicit {

  final AutoB2 two;

  public MyAutoB2TwoExplicit(@Named("two") AutoB2 two) {
    this.two = two;
  }

  public String two() {
    return two.who();
  }

}
