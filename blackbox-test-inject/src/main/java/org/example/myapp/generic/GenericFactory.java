package org.example.myapp.generic;

import org.other.one.OneModule;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class GenericFactory {

  @Bean
  Generic<Integer> one() {

    return new Generic<>() {};
  }
}
