package org.example.coffee.priority.base;

import io.avaje.inject.Priority;

import jakarta.inject.Singleton;

@Singleton
@Priority(7)
public class BBasei implements BaseIface {

  @Override
  public String other() {
    return "b";
  }
}
