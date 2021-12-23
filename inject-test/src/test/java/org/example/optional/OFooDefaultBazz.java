package org.example.optional;

import io.avaje.inject.Secondary;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("forBazz")
@Singleton
@Secondary
public class OFooDefaultBazz implements OFooService {
  @Override
  public String fooey() {
    return "forBazz";
  }
}
