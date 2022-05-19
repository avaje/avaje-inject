package org.example.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import org.example.myapp.HelloData;

@Factory
public class AppConfig {

  @Bean
  HelloData data() {
    return new AppHelloData();
  }

  private static class AppHelloData implements HelloData {

    @Override
    public String helloData() {
      return "AppHelloData";
    }
  }

  @Bean
  Builder newBuilder() {
    return new Builder();
  }

  @Bean
  Generated newGenerated() {
    return new Generated();
  }

  public static class Builder {
  }

  public static class Generated {
  }
}
