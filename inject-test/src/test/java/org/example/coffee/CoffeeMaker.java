package org.example.coffee;

import org.example.coffee.grind.Grinder;

import jakarta.inject.Singleton;

//@InjectModule(name = "coffee-maker", requires = Object.class)
@Singleton
public class CoffeeMaker {

  private final Pump pump;

  private final Grinder grinder;

  public CoffeeMaker(Pump pump, Grinder grinder) {
    this.pump = pump;
    this.grinder = grinder;
  }

  public String makeIt() {
    System.out.println("making it ...");
    System.out.println("grinder:" + grinder.grindBeans());
    pump.pumpWater();
    pump.pumpSteam();
    System.out.println("Done");
    return "done";
  }

  public String prepare() {
    return "prepare: " + grinder.grindBeans();
  }
}
