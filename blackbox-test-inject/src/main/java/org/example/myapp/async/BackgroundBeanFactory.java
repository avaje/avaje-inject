package org.example.myapp.async;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import io.avaje.inject.AsyncBean;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.lang.Nullable;
import jakarta.inject.Named;

@Factory
@AsyncBean
public class BackgroundBeanFactory {

  @Bean
  @Named("factory")
  BackgroundBean lazyInt(@Nullable AtomicInteger intyAtomic) throws InterruptedException {

    System.out.println("StartedInit" + Thread.currentThread().getName());
    return new BackgroundBean(intyAtomic);
  }
}
