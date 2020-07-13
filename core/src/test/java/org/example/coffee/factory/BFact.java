package org.example.coffee.factory;

import org.example.coffee.CoffeeMaker;

public class BFact {

  private final AFact afact;

  private final CoffeeMaker maker;

  private int countClose;
  private int countInit;

  public BFact(AFact afact, CoffeeMaker maker) {
    this.afact = afact;
    this.maker = maker;
  }

  public String b() {
    maker.makeIt();
    return afact.a();
  }

  public void init() {
    countInit++;
  }

  public void close() {
    countClose++;
  }

  int getCountClose() {
    return countClose;
  }

  int getCountInit() {
    return countInit;
  }
}
