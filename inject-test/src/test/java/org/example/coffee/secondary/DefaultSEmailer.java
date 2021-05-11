package org.example.coffee.secondary;

import io.avaje.inject.Secondary;

import javax.inject.Singleton;

@Secondary
@Singleton
public class DefaultSEmailer implements SEmailer {

  @Override
  public String email() {
    return "secondary";
  }
}
