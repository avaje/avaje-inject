package org.example.optional;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class OFooBazz {

  final OFooService service;

  public OFooBazz(@Named("forBazz") OFooService service) {
    this.service = service;
  }

  String fooey() {
    return service.fooey();
  }
}
