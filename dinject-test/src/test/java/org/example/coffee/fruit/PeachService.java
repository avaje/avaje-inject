package org.example.coffee.fruit;

import javax.inject.Singleton;

@Singleton
class PeachService {

  String poke() {
    return "peach";
  }

  String peach(String a, String b, String c) {
    System.out.println("peach> a: " + a + " b:" + b + " c:" + c);
    return " peach";
  }
}
