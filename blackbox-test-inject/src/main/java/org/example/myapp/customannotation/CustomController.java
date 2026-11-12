package org.example.myapp.customannotation;

@MyController
public class CustomController {

  private final CustomService service;

  public CustomController(CustomService service) {
    this.service = service;
  }

  public CustomService service() {
    return service;
  }
}
