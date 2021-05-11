package org.example.optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import javax.annotation.Nullable;

/**
 * Nullable with field injection.
 */
@Singleton
public class AllQue6 {

  private final Que frodo;
  @Inject @Named("sam") Que sam;
  @Inject @Nullable @Named("bilbo") Que bilbo;

  AllQue6(@Named("frodo") Que frodo) {
    this.frodo = frodo;
  }

  String whichSet() {
    return "f:"+frodo+"s:"+sam+"b:"+bilbo;
  }

}
