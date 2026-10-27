package org.example.customannotation.plugin;

import org.example.customannotation.api.AppService;

@AppService
public class GreetService {

  public String greet(String name) {
    return "hello " + name;
  }
}
