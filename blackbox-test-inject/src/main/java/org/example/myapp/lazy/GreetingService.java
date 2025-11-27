package org.example.myapp.lazy;

import io.avaje.inject.Lazy;
import io.avaje.inject.Primary;
import jakarta.inject.Singleton;

interface GreetingService {
  String greet();
}

@Lazy(Lazy.Kind.PROVIDER)
@Singleton
class EnglishGreeting implements GreetingService {
  @Override
  public String greet() {
    return "Hello";
  }
}

@Primary
@Lazy(Lazy.Kind.PROVIDER)
@Singleton
class SpanishGreeting implements GreetingService {
  @Override
  public String greet() {
    return "Hola";
  }
}

@Lazy(Lazy.Kind.PROVIDER)
@Singleton
class Greeter {
  private final GreetingService service;

  Greeter(GreetingService service) {
    this.service = service;
  }

  String run() {
    return service.greet();
  }
}
