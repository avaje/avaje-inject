package org.example.myapp.other;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Factory
class ConfigFactory {

  @Bean
  @Named("map1")
  Map<String, Long> map1() {
    return Map.of("one", 1L, "two", 2L);
  }

  @Bean
  @Named("map2")
  Map<String, String> map2(Map<String, Long> map1) {
    return Map.of("a","hi", "b", "there", "count", String.valueOf(map1.size()));
  }

  @Bean
  Set<UUID> setOfUuid() {
    return Set.of(UUID.randomUUID());
  }

  @Bean
  Set<Long> setOfLong() {
    return Set.of(1L, 2L);
  }
}
