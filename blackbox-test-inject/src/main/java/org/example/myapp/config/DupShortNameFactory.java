package org.example.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class DupShortNameFactory {

  @Bean
  MyDup one() {
    return new MyDup();
  }

  /**
   * Requires fully qualified name as short name of MyDup clashes.
   */
  @Bean
  org.example.myapp.config.MyDup two() {
    return new org.example.myapp.config.MyDup();
  }

  public static class MyDup {

  }
}
