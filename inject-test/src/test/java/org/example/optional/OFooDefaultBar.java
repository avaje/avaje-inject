package org.example.optional;

import io.avaje.inject.Secondary;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Named("forBar")
@Singleton
@Secondary
public class OFooDefaultBar implements OFooService {
  @Override
  public String fooey() {
    return "forBar";
  }
}
