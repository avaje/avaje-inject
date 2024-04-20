package org.example.optional;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * Optional with method injection.
 */
@Singleton
public class AllQue3 {

  private final Que frodo;
  private Que sam;
  private Que bilbo;

  AllQue3(@Named("Frodo") Que frodo) {
    this.frodo = frodo;
  }

  @Inject
  void with(@Named("sam") Optional<Que> sam, @Named("bilbo") Optional<Que> bilbo) {
    this.sam = sam.orElse(null);
    this.bilbo = bilbo.orElse(null);
  }

  String whichSet() {
    return "f:"+frodo+"s:"+sam+"b:"+bilbo;
  }

}
