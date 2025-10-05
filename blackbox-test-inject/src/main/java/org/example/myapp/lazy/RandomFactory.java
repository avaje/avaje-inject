package org.example.myapp.lazy;

import java.security.SecureRandom;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Lazy;

@Lazy
@Factory
public class RandomFactory {
  @Bean
  public SecureRandom secureRandom() {
    return new SecureRandom();
  }
}
