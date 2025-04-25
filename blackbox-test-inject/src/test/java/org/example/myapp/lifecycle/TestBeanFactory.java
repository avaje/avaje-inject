package org.example.myapp.lifecycle;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.test.TestScope;

@TestScope
@Factory
public class TestBeanFactory {
  @Bean
  public GreetingService greetingService() {
    return new GreetingService();
  }
}
