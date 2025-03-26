package org.example.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

import java.util.Optional;

@Factory
class CFactory {

  @Bean @Named("base")
  CFace base() {
    return new TheCFace("base");
  }

  @Bean @Named("optional")
  Optional<CFace> optional() {
    return Optional.of(new TheCFace("optional"));
  }

  static final class TheCFace implements CFace {

    private final String msg;

    TheCFace(String msg) {
      this.msg = msg;
    }

    @Override
    public String msg() {
      return msg;
    }
  }
}
