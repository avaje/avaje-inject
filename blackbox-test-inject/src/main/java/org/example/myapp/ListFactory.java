package org.example.myapp;

import java.util.List;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.PostConstruct;

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

  @Bean
  List<Some> multiple() {
    return List.of(new Some("m1"), new Some("m2"), new Some("m3"));
  }

  @Bean
  Some one() {
    return new Some("s1");
  }

  public static class Some {

    public final String name;

    public Some(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }

    @PostConstruct
    void shutdown() {

    }
  }
}
