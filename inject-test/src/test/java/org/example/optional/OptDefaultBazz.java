package org.example.optional;

import io.avaje.inject.Secondary;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("defaultBazz")
@Secondary
public class OptDefaultBazz implements OptionalService {
  @Override
  public String hi() {
    return "defaultBazz";
  }
}
