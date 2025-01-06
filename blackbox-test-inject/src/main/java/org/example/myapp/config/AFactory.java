package org.example.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import org.example.myapp.MyNestedDestroy;

import java.io.IOException;

@Factory
class AFactory {

  @Bean(initMethod = "start", destroyMethod = "reaper().stop()")
  MyNestedDestroy lifecycle2() {
    return new MyNestedDestroy();
  }

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

  @Bean
  AFoo buildAfoo() throws IOException {
    return new AFoo();
  }

  static class I0 implements A0.Builder {

  }
  static class I1 implements A1.Builder {

  }

  static class AFoo {

  }
}
