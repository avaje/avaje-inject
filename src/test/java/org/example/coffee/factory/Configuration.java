package org.example.coffee.factory;

import io.kanuka.Bean;
import io.kanuka.Factory;
import org.example.coffee.CoffeeMaker;
import org.example.coffee.grind.AMusher;

import javax.inject.Inject;

@Factory
public class Configuration {

  private final StartConfig startConfig;

  @Inject
  public Configuration(StartConfig startConfig) {
    this.startConfig = startConfig;
  }

  @Bean
  public AFact buildA() {

    String userHome = System.getProperty("user.home");
    return new AFact(userHome);
  }

  @Bean
  public BFact buildB(AFact afact, CoffeeMaker maker, AMusher musher) {

    return new BFact(afact, maker);
  }
}
