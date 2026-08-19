package org.example.globalscope;

import jakarta.inject.Singleton;

@Singleton
public class DefaultGreeter implements Greeter {
  @Override
  public String greet() {
    return "hello";
  }
}
