package org.example.myapp.assist;

import io.avaje.inject.Component;

@Component
public class CssThing {

  private final CssFactory factory;

  CssThing(CssFactory factory) {
    this.factory = factory;
  }

  public String scan(String file) {
    Scanner scanner = factory.scanner(file);
    return scanner.scan();
  }
}
