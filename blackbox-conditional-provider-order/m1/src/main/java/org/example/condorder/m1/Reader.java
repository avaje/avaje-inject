package org.example.condorder.m1;

import jakarta.inject.Singleton;
import org.example.condorder.api.Greeting;

@Singleton
public class Reader {
  public final Greeting greeting;

  Reader(Greeting greeting) {
    this.greeting = greeting;
  }

  public String read() {
    return greeting.hello();
  }
}
