package org.example.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

import java.util.Optional;

@Factory
public class BFactory {

  @Named
  @Bean
  BFace one() {
    return new TheBFace("one");
  }

  @Named
  @Bean
  BFace two() {
    return new TheBFace("two");
  }

  @Named
  @Bean
  Optional<BFace> three() {
    return Optional.of(new TheBFace("three"));
  }


  static class TheBFace implements BFace {

    private final String msg;

    TheBFace(String msg) {
      this.msg = msg;
    }

    @Override
    public String hi() {
      return msg;
    }
  }
}
