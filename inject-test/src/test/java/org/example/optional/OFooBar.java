package org.example.optional;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class OFooBar {

  final OFooService service;

  public OFooBar(@Named("forBar") OFooService service) {
    this.service = service;
  }

  String fooey() {
    return service.fooey();
  }
}
