package org.example.coffee.priority.base;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Priority;

@Factory
public class PriorityFactory {

  @Bean
  @Priority(69)
  BaseIface iface() {
    return new DBasei();
  }

  public static class DBasei implements BaseIface {

    @Override
    public String other() {
      return "b";
    }
  }
}
