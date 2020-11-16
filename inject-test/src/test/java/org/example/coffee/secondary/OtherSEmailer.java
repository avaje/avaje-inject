package org.example.coffee.secondary;

import jakarta.inject.Singleton;

@Singleton
public class OtherSEmailer implements SEmailer {

  @Override
  public String email() {
    return "other";
  }
}
