package org.example.myapp.config;

import io.avaje.inject.Bean;
import io.avaje.inject.Component;
import io.avaje.inject.Factory;
import io.avaje.inject.Prototype;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
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

  @Prototype
  @Bean
  public Builder newBuilder() {
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

  @Component
  public static class BuilderUser {

    final Provider<Builder> builderProvider;

    @Inject
    public BuilderUser(Provider<Builder> builderProvider) {
      this.builderProvider = builderProvider;
    }

    public Builder createBuilder() {
      return builderProvider.get();
    }
  }
}
