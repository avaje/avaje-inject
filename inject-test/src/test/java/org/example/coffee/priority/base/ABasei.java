package org.example.coffee.priority.base;

import io.avaje.inject.Priority;

import jakarta.inject.Singleton;

@Singleton
@Priority(42)
public class ABasei implements BaseIface {

  @Override
  public String other() {
    return "a";
  }
}
