package io.avaje.inject.generator.models.valid.conditions;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.RequiresProperty;

@Factory
class MyServiceFactory {

  @Bean
  @RequiresProperty(value = "service.type", equalTo = "one")
  MyService myServiceOne() {
        return new MyServiceOne();
    }

  @Bean
  @RequiresProperty(value = "service.type", equalTo = "two")
  MyService myServiceTwo() {
        return new MyServiceTwo();
    }
}