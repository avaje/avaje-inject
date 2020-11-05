package org.example.coffee.priority.custom;

import jakarta.inject.Singleton;

@Singleton
@CustomPriority(7)
public class BOtheri implements OtherIface {

  @Override
  public String other() {
    return "b";
  }
}
