package org.example.optional;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import javax.annotation.Nullable;

/**
 * Nullable with method injection.
 */
@Singleton
public class AllQue4 {

  private final Que frodo;
  private Que sam;
  private Que bilbo;

  AllQue4(@Named("frodo") Que frodo) {
    this.frodo = frodo;
  }

  @Inject
  void with(@Named("sam") Que sam, @Nullable @Named("bilbo") Que bilbo) {
    this.sam = sam;
    this.bilbo = bilbo;
  }

  String whichSet() {
    return "f:"+frodo+"s:"+sam+"b:"+bilbo;
  }

}
