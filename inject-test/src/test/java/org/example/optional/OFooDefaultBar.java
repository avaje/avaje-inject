package org.example.optional;

import io.avaje.inject.Secondary;
import javax.inject.Named;
import javax.inject.Singleton;

@Named("forBar")
@Singleton
@Secondary
public class OFooDefaultBar implements OFooService {
  @Override
  public String fooey() {
    return "forBar";
  }
}
