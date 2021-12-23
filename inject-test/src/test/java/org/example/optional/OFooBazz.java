package org.example.optional;

import javax.inject.Named;
import javax.inject.Singleton;

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
