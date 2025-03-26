package io.avaje.inject.generator.models.valid;

import java.util.Map;
import java.util.Optional;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

@Factory
public class ConfigFactory {
  @Bean
  @Named("map1")
  Map<String, Long> map1() {
    return Map.of();
  }

  @Bean
  @Named("map2")
  Map<String, String> map2(@Named("map1") Map<String, Long> map1) {
    return Map.of();
  }

  @Bean
  @Named("optionalA0")
  Optional<A0> build() {
    return Optional.empty();
  }
}
