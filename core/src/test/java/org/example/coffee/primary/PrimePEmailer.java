package org.example.coffee.primary;

import io.dinject.annotation.Primary;

import javax.inject.Singleton;

@Primary
@Singleton
public class PrimePEmailer implements PEmailer {
  @Override
  public String email() {
    return "primary";
  }
}
