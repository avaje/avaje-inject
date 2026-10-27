package org.example.customannotation.classpath;

import org.example.customannotation.api.FxController;

@FxController
public class GreetController {

  private final GreetService service;

  public GreetController(GreetService service) {
    this.service = service;
  }

  public GreetService service() {
    return service;
  }
}
