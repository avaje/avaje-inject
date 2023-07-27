package io.avaje.inject.generator.models.valid.generic;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class MyConsumerFactory {

  @Bean
  MyConsumer createTest() {
    return new MyConsumer();
  }
}
