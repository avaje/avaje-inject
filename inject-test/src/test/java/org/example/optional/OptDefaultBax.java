package org.example.optional;

import io.avaje.inject.Secondary;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("default")
@Secondary
public class OptDefaultBax implements OptionalService {
  @Override
  public String hi() {
    return "defaultOptionalBax";
  }
}
