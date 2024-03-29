package io.avaje.inject.generator.models.valid.provider;

import java.util.function.Supplier;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Secondary;
import jakarta.inject.Provider;

@Factory
public class FactoryProvider {

  @Bean
  @Secondary
  Provider<Grinder> grinder() {
    return Grinder::new;
  }

  @Bean
  Provider<Supplier<String>> supply() {
    return () -> () -> "";
  }
}
