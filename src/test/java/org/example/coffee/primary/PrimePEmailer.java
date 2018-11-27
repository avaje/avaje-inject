package org.example.coffee.primary;

import io.dinject.Primary;
import io.dinject.Secondary;

import javax.inject.Named;
import javax.inject.Singleton;

@Named("prime")
//@Primary
//@Secondary
@Singleton
public class PrimePEmailer implements PEmailer {
  @Override
  public String email() {
    return "primary";
  }
}
