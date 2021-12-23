package org.example.optional;

import io.avaje.inject.Secondary;
import javax.inject.Named;
import javax.inject.Singleton;

@Named("forBazz")
@Singleton
@Secondary
public class OFooDefaultBazz implements OFooService {
  @Override
  public String fooey() {
    return "forBazz";
  }
}
