package org.example.coffee.fruit;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AppleService {

  @Inject
  BananaService bananaService;

  PeachService peachService;

  @Inject
  void setPeachService(PeachService peachService) {
    this.peachService = peachService;
  }

  String ban() {
    return bananaService.ban("hello");
  }

  void apple(String a, String b, String c) {
    System.out.println("apple> a: " + a + " b:" + b + " c:" + c);
    bananaService.banana(a, b, c);
    peachService.peach(a, b, c);
  }

  public void passIt(String arg) {
    peachService.callIt(arg);
  }
}
