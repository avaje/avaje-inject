package org.example.coffee.provider;

import java.util.function.Supplier;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Secondary;
import jakarta.inject.Named;
import jakarta.inject.Provider;

@Factory
public class FactoryProvider {

  @Bean
  @Secondary
  @Named("second")
  Provider<String> second() {
    return () -> "Nah, I'd win";
  }

  @Bean
  Provider<Supplier<String>> supply() {
    return () -> () -> "Stand proud Provider, you were strong";
  }
}
