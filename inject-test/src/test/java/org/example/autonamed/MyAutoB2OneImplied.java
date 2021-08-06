package org.example.autonamed;

import javax.inject.Singleton;

@Singleton
public class MyAutoB2OneImplied {

  final AutoB2 one;

  public MyAutoB2OneImplied(AutoB2 one) {
    this.one = one;
  }

  public String one() {
    return one.who();
  }

}
