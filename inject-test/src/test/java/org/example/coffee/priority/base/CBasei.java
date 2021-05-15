package org.example.coffee.priority.base;

import io.avaje.inject.Priority;
import jakarta.inject.Singleton;

@Singleton
@Priority(1)
public class CBasei implements BaseIface {

  @Override
  public String other() {
    return "c";
  }
}
