package org.example.myapp.generic;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Factory
class MyConsumerFactory {

  @Bean
  MyConsumer createTest() {
    return new MyConsumer();
  }

  @Bean
  Map<String, MyConsumer> genericMap(BiConsumer<MyA, MyB> consumer) {
    return new HashMap<>();
  }

  @Bean
  Aldrich<String, String> bean() {
    return new Aldrich<>() {
      @Override
      public void accept(String s, String s2) {

      }

      @Override
      public void accept(BiConsumer<String, String> stringStringBiConsumer) {

      }
    };
  }
}
