package org.example.myapp.lazy;

import java.security.SecureRandom;
import java.util.Random;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;

@Lazy(useProxy = false)
@Factory
public class RandomFactory {
  @Bean
  public Random secureRandom() {
    return new SecureRandom();
  }
}
