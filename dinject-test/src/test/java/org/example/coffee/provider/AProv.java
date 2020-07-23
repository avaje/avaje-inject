package org.example.coffee.provider;

import org.example.coffee.grind.Grinder;

public class AProv {

  private final Grinder grinder;

  public AProv(Grinder grinder) {
    this.grinder = grinder;
  }

  public String a() {
    return grinder.grindBeans();
  }
}
