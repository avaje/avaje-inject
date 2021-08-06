package org.example.autonamed;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class MyAutoName2 {

  final AutoIface one;

  public MyAutoName2(@Named("one") AutoIface one) {
    this.one = one;
  }

  public String who() {
    return one.who();
  }
}
