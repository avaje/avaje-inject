package org.example.coffee.fruit;

import javax.inject.Singleton;

import java.util.Objects;

@Fruit
@Singleton
public class PeachService {

  String poke() {
    return "peach";
  }

  String peach(String a, String b, String c) {
    System.out.println("peach> a: " + a + " b:" + b + " c:" + c);
    return " peach";
  }

  public void callIt(String arg) {
    Objects.requireNonNull(arg);
  }
}
