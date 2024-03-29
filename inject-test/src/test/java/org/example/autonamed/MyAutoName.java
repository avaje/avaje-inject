package org.example.autonamed;

import jakarta.inject.Singleton;

@Singleton
public class MyAutoName {

  final AutoIface one;

  public MyAutoName(AutoIface one) {
    this.one = one;
  }

  public String who() {
    return one.who();
  }
}
