package org.example.coffee.core;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;

@Singleton
public class Steamer {

  String makeSteam() {
    return "Steam";
  }

  @PreDestroy
  void onStop() {
    System.out.println("Chucking the toys");
  }

}
