package org.example.coffee.factory;

import org.example.coffee.CoffeeMaker;

public class BFact {

  private final AFact afact;

  private final CoffeeMaker maker;

  public BFact(AFact afact, CoffeeMaker maker) {
    this.afact = afact;
    this.maker = maker;
  }

  public String b() {
    maker.makeIt();
    return afact.a();
  }

}
