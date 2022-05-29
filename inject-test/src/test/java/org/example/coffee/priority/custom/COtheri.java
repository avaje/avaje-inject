package org.example.coffee.priority.custom;

import jakarta.inject.Singleton;

@Singleton
@CustomPriority(1)
public class COtheri implements OtherIface {

  @Override
  public String other() {
    return "c";
  }
}
