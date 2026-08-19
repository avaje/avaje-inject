package org.example.condorder.m2;

import io.avaje.inject.RequiresProperty;
import jakarta.inject.Singleton;
import org.example.condorder.api.Greeting;

@Singleton
@RequiresProperty(value = "greet.mode", equalTo = "alt")
public class AltGreeting implements Greeting {
  @Override
  public String hello() {
    return "alt";
  }
}
