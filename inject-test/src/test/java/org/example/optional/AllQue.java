package org.example.optional;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Optional;

/**
 * Optional as constructor arguments.
 */
@Singleton
public class AllQue {

  private final Optional<Que> frodo;
  private final Optional<Que> sam;
  private final Optional<Que> bilbo;

  AllQue(@Named("frodo") Optional<Que> frodo, @Named("sam") Optional<Que> sam, @Named("bilbo") Optional<Que> bilbo) {
    this.frodo = frodo;
    this.sam = sam;
    this.bilbo = bilbo;
    System.out.println("bazz");
  }

  String whichSet() {
    return "f:"+frodo+"s:"+sam+"b:"+bilbo;
  }

  public String frodoPush(String msg) {
    return frodo.get().push(msg);
  }
}
