package org.example.coffee.grind;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Grinder {

  final AMusher aMusher;

  /**
   * Can use field injection (but can't be private)
   */
  @Inject
  BMusher bMusher;

  public Grinder(AMusher aMusher) {
    this.aMusher = aMusher;
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
