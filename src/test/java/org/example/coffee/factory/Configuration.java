package org.example.coffee.factory;

import io.kanuka.Bean;
import io.kanuka.Factory;
import org.example.coffee.CoffeeMaker;

import javax.inject.Inject;

/**
 * Factory bean (ala Micronaut @Factory or Spring @Configuration)
 */
@Factory
class Configuration {

  private final StartConfig startConfig;

  /**
   * Factory beans can have dependencies.
   */
  @Inject
  Configuration(StartConfig startConfig) {
    this.startConfig = startConfig;
  }

  @Bean
  AFact buildA() {

    String userHome = System.getProperty("user.home");
    return new AFact(userHome);
  }

  /**
   * Builder method that has dependencies.
   */
  @Bean
  BFact buildB(AFact afact, CoffeeMaker maker) {
    return new BFact(afact, maker);
  }
}
