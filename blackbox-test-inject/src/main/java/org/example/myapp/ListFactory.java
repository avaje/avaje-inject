package org.example.myapp;

import java.util.List;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class ListFactory {

  @Bean
  List<String> test() {
    return List.of("test1", "test2");
  }

  @Bean
  String test3() {
    return "test3";
  }
}
