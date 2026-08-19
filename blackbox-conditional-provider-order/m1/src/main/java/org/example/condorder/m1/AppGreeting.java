package org.example.condorder.m1;

import io.avaje.inject.RequiresProperty;
import jakarta.inject.Singleton;
import org.example.condorder.api.Greeting;

@Singleton
@RequiresProperty(value = "greet.mode", equalTo = "app")
public class AppGreeting implements Greeting {
  @Override
  public String hello() {
    return "app";
  }
}
