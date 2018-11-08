package org.example.coffee.grind;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class Grinder {

  @PostConstruct
  void onStart() {
    System.out.println("firing up the grinding machine !!!");
  }

  public String grindBeans() {
    return "mush mush beans";
  }


}
