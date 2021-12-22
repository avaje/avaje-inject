package org.example.coffee.factory;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class FactoryForBeanWithGenericInterface {
  @Bean
  public BeanWithGenericInterface create() {
    return new BeanWithGenericInterface();
  }
}
