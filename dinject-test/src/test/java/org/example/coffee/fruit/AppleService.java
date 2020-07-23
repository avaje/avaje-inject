package org.example.coffee.fruit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class AppleService {

  @Inject
  BananaService bananaService;

  @Inject
  PeachService peachService;

  String ban() {
    return bananaService.ban("hello");
  }


  void apple(String a, String b, String c) {
    System.out.println("apple> a: " + a + " b:" + b + " c:" + c);
    bananaService.banana(a, b, c);
    peachService.peach(a, b, c);
  }
}
