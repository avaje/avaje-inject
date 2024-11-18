package org.example.myapp.async;

import io.avaje.inject.AsyncBean;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;

@Factory
@AsyncBean
public class BackgroundBeanFactory {

  @Bean
  @Named("factory")
  BackgroundBean lazyInt() throws InterruptedException {

    return new BackgroundBean();
  }
}
