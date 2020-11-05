package org.example.coffee.fruit;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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
