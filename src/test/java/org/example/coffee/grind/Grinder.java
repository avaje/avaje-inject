package org.example.coffee.grind;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Grinder {

  final AMusher aMusher;

  //@Inject
  BMusher bMusher;

  public Grinder(AMusher aMusher, BMusher bMusher) {
    this.aMusher = aMusher;
    this.bMusher = bMusher;
  }

  @PostConstruct
  void onStart() {
    System.out.println("firing up the grinding machine !!!");
    System.out.println("amush:" + aMusher.mush() + " bmush:" + bMusher.mush());
  }

  public String grindBeans() {
    return "mush mush beans";
  }


}
