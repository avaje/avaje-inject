package org.example.myapp;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class ExampleServiceTest {

  @Test
  void exercise() {
    try (BeanScope beanScope = BeanScope.newBuilder().build()) {
      ExampleService exampleService = beanScope.get(ExampleService.class);

      exampleService.other("foo", 42);
      exampleService.runOnly("foo");
      exampleService.withParamAtomic(new AtomicLong(42));
      exampleService.withParamImport(new ConcurrentHashMap<>());
    }
  }
}
