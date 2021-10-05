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
}
