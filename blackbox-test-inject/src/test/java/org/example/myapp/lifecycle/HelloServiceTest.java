package org.example.myapp.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.avaje.inject.test.InjectTest;
import jakarta.inject.Inject;

@InjectTest
class HelloServiceTest {

  @Inject GreetingService service;

  @Test
  void testGreetingMessage() {
    assertEquals("Hello from GreetingService!", service.getGreeting());
  }
}
