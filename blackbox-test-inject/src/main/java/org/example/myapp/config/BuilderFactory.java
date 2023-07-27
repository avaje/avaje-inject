package org.example.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class BuilderFactory {

  @Bean
  void consume0(A0.Builder aBuilder) {
    aBuilder.hashCode();
  }

  @Bean
  void consume1(A1.Builder aBuilder) {
    aBuilder.hashCode();
  }
}
