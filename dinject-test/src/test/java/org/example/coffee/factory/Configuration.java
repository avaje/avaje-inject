package org.example.coffee.factory;

import io.dinject.annotation.Bean;
import io.dinject.annotation.Factory;
import org.example.coffee.CoffeeMaker;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
