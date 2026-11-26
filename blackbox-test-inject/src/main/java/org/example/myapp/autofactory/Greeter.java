package org.example.myapp.autofactory;

import jakarta.inject.Singleton;

@Singleton
public class Greeter {

  String greet(String value) {
    return "hello " + value;
  }
}
