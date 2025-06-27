package org.example.myapp;

import java.util.List;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.PostConstruct;
import io.avaje.inject.PreDestroy;
import jakarta.inject.Named;

@Factory
public class ListFactory {

  @Bean
  List<String> test(List<Cloneable> emptyList) {
    return List.of("test1", "test2");
  }

  @Bean
  String test3() {
    return "test3";
  }

  @Bean(initMethod = "initMe")
  List<Some> multiple() {
    return List.of(new Some("m1"), new Some("m2"), new Some("m3"));
  }

  @Bean(initMethod = "postInit")
  Some one() {
    return new Some("s1");
  }

  @Named("multipleVanilla")
  @Bean
  List<Some2> multipleVanilla() {
    return List.of(new Some2("v1"), new Some2("v2"));
  }

  @Bean
  Some2 oneVanilla() {
    return new Some2("v3");
  }

  @Bean
  List<Some3> multiple3() {
    return List.of(new Some3("31"), new Some3("32"));
  }

  @Bean
  List<Some4> multiple4() {
    return List.of(new Some4("41"), new Some4("42"));
  }

  public static class Some {

    public final String name;
    public String other;

    public Some(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }

    public String other() {
      return other;
    }

    public void initMe() {
      other = name + "initMe";
    }

    @PostConstruct
    public void postInit() {
      other = name + "postInit";
    }

    @PreDestroy
    void shutdown() {
      other = "stop";
    }
  }

  public static class Some2 {

    public final String name;

    public Some2(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }
  }

  public static class Some3 {

    public final String name;

    public Some3(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }
    @PreDestroy
    void shutdown() {

    }
  }

  public static class Some4 {

    public final String name;

    public Some4(String name) {
      this.name = name;
    }

    public String name() {
      return name;
    }
    @PostConstruct
    void myInitMethod() {
    }
  }
}
