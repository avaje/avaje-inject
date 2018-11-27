package org.example.coffee.secondary;

import javax.inject.Singleton;

@Singleton
public class OtherSEmailer implements SEmailer {

  @Override
  public String email() {
    return "other";
  }
}
