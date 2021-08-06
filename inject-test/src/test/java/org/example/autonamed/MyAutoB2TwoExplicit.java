package org.example.autonamed;

import javax.inject.Named;
import javax.inject.Singleton;

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
