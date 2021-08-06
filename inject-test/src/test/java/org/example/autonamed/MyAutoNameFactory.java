package org.example.autonamed;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

@Factory
public class MyAutoNameFactory {

  @Bean
  AutoIface one() {
    return new DAutoIface("one");
  }

  @Named("one")
  @Bean
  AutoB2 oneB2() {
    return new DAutoB2("oneB2");
  }

  @Named("two")
  @Bean
  AutoB2 twoB2() {
    return new DAutoB2("twoB2");
  }

  static class DAutoB2 implements AutoB2 {
    private final String name;
    DAutoB2(String name) {
      this.name = name;
    }
    @Override
    public String who() {
      return name;
    }
  }

  static class DAutoIface implements AutoIface {

    private final String name;
    DAutoIface(String name) {
      this.name = name;
    }

    @Override
    public String who() {
      return name;
    }
  }
}
