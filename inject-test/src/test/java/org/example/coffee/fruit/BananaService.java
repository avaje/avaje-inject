package org.example.coffee.fruit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Fruit
@Singleton
class BananaService {

  @Inject
  PeachService peachService;

  String banana(String a, String b, String c) {
    System.out.println("banana> a: " + a + " b:" + b + " c:" + c);
    return " banana " + peachService.poke();
  }

  String ban(String hello) {
    return hello + " banana " + peachService.poke();
  }
}
