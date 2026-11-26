package org.example.myapp.customannotation;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class CustomFactory {

  public static class Widget {
  }

  @Bean
  @MyController
  Widget widget() {
    return new Widget();
  }
}
