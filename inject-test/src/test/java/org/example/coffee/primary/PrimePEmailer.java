package org.example.coffee.primary;

import io.avaje.inject.Primary;

import javax.inject.Singleton;

@Primary
@Singleton
public class PrimePEmailer implements PEmailer {
  @Override
  public String email() {
    return "primary";
  }
}
