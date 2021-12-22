package org.example.generic;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class MyFuncFactory {

  @Bean
  MyFunc createTest() {
    return new MyFunc();
  }
}
