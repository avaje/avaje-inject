package org.example.coffee.factory;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import org.example.coffee.CoffeeMaker;

import javax.inject.Inject;

/**
 * Factory bean (ala Micronaut @Factory or Spring @Configuration)
 */
@Factory
class Configuration {

  private final StartConfig startConfig;
  private int countInit;
  private int countClose;

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
  @Bean(initMethod = "init", destroyMethod = "close")
  BFact buildB(AFact afact, CoffeeMaker maker) {
    return new BFact(afact, maker);
  }

  @PostConstruct
  void initFactory() {
    countInit++;
  }

  @PreDestroy
  void closeFactory() {
    countClose++;
  }

  int getCountInit() {
    return countInit;
  }

  int getCountClose() {
    return countClose;
  }
}
