package io.avaje.inject.generator.models.valid.generic;

import java.util.Map;
import java.util.function.BiConsumer;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.generator.models.valid.A0;

@Factory
public class MyConsumerFactory {

  @Bean
  MyConsumer createTest() {
    return new MyConsumer();
  }

  @Bean
  Map<String, MyConsumer> genericMap(
      BiConsumer<A0, io.avaje.inject.generator.models.valid.nested.A0> consumer) {
    return null;
  }

  @Bean
  Aldrich<String, String> bean() {

    return null;
  }
}
