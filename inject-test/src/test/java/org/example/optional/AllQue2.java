package org.example.optional;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * Optional as constructor arguments (orElse(null)).
 */
@Singleton
public class AllQue2 {

  @Inject @Named("frodo") Que frodo;
  @Inject @Named("sam") Optional<Que> sam;
  @Inject @Named("bilbo") Optional<Que> bilbo;

  String whichSet() {
    return "f:"+frodo+"s:"+sam+"b:"+bilbo;
  }

}
