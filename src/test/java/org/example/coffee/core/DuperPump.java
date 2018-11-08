package org.example.coffee.core;

import org.example.coffee.Pump;

import javax.inject.Singleton;

@Singleton
public class DuperPump implements Pump {

  private final Steamer steamer;

  DuperPump(Steamer steamer) {
    this.steamer = steamer;
  }

  @Override
  public void pumpWater() {
    System.out.println("pumping water ...");
  }

  @Override
  public void pumpSteam() {
    System.out.println("pumping steam, lots of  " + steamer.makeSteam());
  }
}
