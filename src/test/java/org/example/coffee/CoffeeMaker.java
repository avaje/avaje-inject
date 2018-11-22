package org.example.coffee;

import io.dinject.ContextModule;
import org.example.coffee.grind.Grinder;

import javax.inject.Singleton;

@ContextModule(name = "doo", dependsOn = {"x", "y"})
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
    grinder.grindBeans();
    pump.pumpWater();
    pump.pumpSteam();
    System.out.println("Done");
    return "done";
  }
}
