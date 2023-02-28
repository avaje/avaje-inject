package org.example.autonamed;

import io.avaje.inject.Component;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Component.Import(MyAutoB2.class)
public class MyAutoB2Explicit {

  final AutoB2 one;
  final AutoB2 two;

  public MyAutoB2Explicit(@Named("one") AutoB2 one, @Named("two") AutoB2 two) {
    this.one = one;
    this.two = two;
  }

  public String one() {
    return one.who();
  }

  public String two() {
    return two.who();
  }
}
