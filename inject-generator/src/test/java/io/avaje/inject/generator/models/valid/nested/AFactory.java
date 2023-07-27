package io.avaje.inject.generator.models.valid.nested;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
class AFactory {

  @Bean
  A0.Builder build0() {
    return new I0();
  }

  @Bean
  A1.Builder build1() {
    return new I1();
  }

  @Bean
  void andUse(A1.Builder a1Build) {
    a1Build.hashCode();
  }

  @Bean
  void ad(A0.Builder b1, A1.Builder b2) {
    b1.hashCode();
    b2.hashCode();
  }

  static class I0 implements A0.Builder {}

  static class I1 implements A1.Builder {}
}
