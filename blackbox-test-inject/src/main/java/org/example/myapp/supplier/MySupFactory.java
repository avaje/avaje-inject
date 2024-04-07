package org.example.myapp.supplier;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Factory
public final class MySupFactory {

  private final AtomicInteger counter = new AtomicInteger();

  @Bean
  Supplier<SupFoo> supplyIt() {
    return () -> new SupFoo(counter.incrementAndGet());
  }

  public static class SupFoo {

    private final int id;

    SupFoo(int id) {
      this.id = id;
    }

    public int id() {
      return id;
    }
  }
}
