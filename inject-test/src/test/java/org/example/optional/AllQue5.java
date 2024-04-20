package org.example.optional;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.Nullable;

/**
 * Nullable with constructor injection.
 */
@Singleton
public class AllQue5 {

  private final Que frodo;
  private final Que sam;
  private final Que bilbo;

  /**
   * Use Nullable with constructor argument.
   */
  AllQue5(@Named("Frodo") Que frodo, @Named("sam") Que sam, @Nullable @Named("bilbo") Que bilbo) {
    this.frodo = frodo;
    this.sam = sam;
    this.bilbo = bilbo;
  }

  String whichSet() {
    return "f:" + frodo + "s:" + sam + "b:" + bilbo;
  }

}
