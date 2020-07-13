package org.example.coffee.primary;

import io.dinject.Secondary;

import javax.inject.Singleton;

//@Secondary
@Singleton
public class OtherPEmailer implements PEmailer {

  @Override
  public String email() {
    return "other";
  }
}
